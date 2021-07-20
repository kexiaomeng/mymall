package com.tracy.mymall.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.tracy.mymall.common.constant.OrderConst;
import com.tracy.mymall.common.dto.SkuHasStockDto;
import com.tracy.mymall.common.enums.OrderStatusEnum;
import com.tracy.mymall.common.utils.R;
import com.tracy.mymall.common.vo.MemberEntityVo;
import com.tracy.mymall.common.vo.WareLockVo;
import com.tracy.mymall.order.configuration.AliPayTemplate;
import com.tracy.mymall.order.entity.OrderItemEntity;
import com.tracy.mymall.order.feign.MyMallCartFeignService;
import com.tracy.mymall.order.feign.MyMallMemberFeignService;
import com.tracy.mymall.order.feign.MyMallProductFeignService;
import com.tracy.mymall.order.feign.MyMallWareFeignService;
import com.tracy.mymall.order.inteceptor.OrderInteceptor;
import com.tracy.mymall.order.kafka.KafkaConsumer;
import com.tracy.mymall.order.kafka.KafkaProducer;
import com.tracy.mymall.order.service.OrderItemService;
import com.tracy.mymall.order.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tracy.mymall.common.utils.PageUtils;
import com.tracy.mymall.common.utils.Query;

import com.tracy.mymall.order.dao.OrderDao;
import com.tracy.mymall.order.entity.OrderEntity;
import com.tracy.mymall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {
    private static final String REDIS_ATOMIC_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
    @Autowired
    private MyMallCartFeignService myMallCartFeignService;

    @Autowired
    private MyMallMemberFeignService myMallMemberFeignService;

    @Autowired
    private MyMallWareFeignService myMallWareFeignService;

    @Autowired
    private MyMallProductFeignService myMallProductFeignService;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private AliPayTemplate aliPayTemplate;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 涉及到多个远程服务，考虑异步处理
     * @return
     */
    @Override
    public OrderConfirmVo confirmOrder() {
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();

        MemberEntityVo memberEntityVo = OrderInteceptor.loginUser.get();

        // 1. 获取用户收货地址
        // feign远程调用时候会丢失请求头，因为构建的是新的请求，所以需要在feigin的拦截器中将页面过来的请求的请求头封装到feign构建的请求中
        // 在配置中配置拦截器的bean对象，spring会自动注入
        // 异步请求会丢失threadlocal，需要重新设置
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> memberAddress = myMallMemberFeignService.getMemberAddress(memberEntityVo.getId());
            orderConfirmVo.setAddress(memberAddress);
        }, threadPoolExecutor);

        // 2. 获取用户订单项目
        // feign远程调用时候会丢失请求头，因为构建的是新的请求，所以需要在feigin的拦截器中将页面过来的请求的请求头封装到feign构建的请求中
        // 在配置中配置拦截器的bean对象，spring会自动注入
        CompletableFuture<Void> orderItemFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> checkedItems = myMallCartFeignService.getCheckedItems();
            orderConfirmVo.setItems(checkedItems);
        }, threadPoolExecutor)
                .thenRunAsync(() -> {
                    // 2.1 查询是否有库存,将信息记录
                    List<Long> collect = orderConfirmVo.getItems().stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
                    R r = myMallWareFeignService.hasStock(collect);
                    if (r.getCode() == 0) {
                        Object data = r.get("data");
                        String value = JSON.toJSONString(data);
                        List<SkuHasStockDto> skuHasStockDtos = JSON.parseObject(value, new TypeReference<List<SkuHasStockDto>>() {});
                        Map<Long, Boolean> collect1 = skuHasStockDtos.stream().collect(Collectors.toMap(SkuHasStockDto::getSkuId, SkuHasStockDto::getHasStock));
                        orderConfirmVo.setStocks(collect1);
                    }

                }, threadPoolExecutor);


        // 3. 获取积分信息
        orderConfirmVo.setIntegration(memberEntityVo.getIntegration());
        // 4. 计算价格,价格在调用属性的get方法的时候会自动计算，不需要手动计算
        // 5. 设置防重令牌，放到redis中，在用户提交订单的时候校验，防止重复提交
        String orderToken = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(OrderConst.ORDER_TOKEN + memberEntityVo.getId(), orderToken, 30, TimeUnit.MINUTES);
        orderConfirmVo.setOrderToken(orderToken);
        try {
            CompletableFuture.allOf(orderItemFuture, addressFuture).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return orderConfirmVo;
    }

    @Override
//    @GlobalTransactional(rollbackFor = RuntimeException.class) // 开启一个分布式事务
    @Transactional(rollbackFor = RuntimeException.class) // 保存订单是一个事务
    public OrderSubmitRespVo submitOrder(OrderSubmitVo orderSubmitVo) {
        OrderSubmitRespVo orderSubmitRespVo = new OrderSubmitRespVo();
        MemberEntityVo memberEntityVo = OrderInteceptor.loginUser.get();
        // 1. 验证防重令牌
        String orderToken = orderSubmitVo.getOrderToken();

        Long execute = redisTemplate.execute(new DefaultRedisScript<>(REDIS_ATOMIC_SCRIPT, Long.class), Arrays.asList(OrderConst.ORDER_TOKEN+memberEntityVo.getId()), orderToken);
        // 返回0表示执行失败
        if (execute == null || execute == 0) {
            orderSubmitRespVo.setCode(1);
        }else {
            orderSubmitRespVo.setCode(0);
            // 生成订单
            OrderTo orderTo  = createOrderTo(orderSubmitVo);
            orderSubmitRespVo.setOrder(orderTo.getOrder());

            // 创建订单
                // 创建订单项
                    // 订单号
                    // spu
                    // sku
                    // jifenxinxi
                    // 优惠信息
            // 生成订单号
            // 根据addrid获取收获地址
                // 获取收货地址
            // 获取运费
            // 创建订单

            // 验价,对比页面传输的和后台实际的价格偏差

            BigDecimal payPriceWeb = orderSubmitVo.getPayPrice();
            BigDecimal payPriceReal = orderTo.getPayPrice();

            if(Math.abs(payPriceReal.subtract(payPriceWeb).doubleValue()) < 0.01){
                // 如果验价成功
                // 保存订单和订单项
                saveOrder(orderTo);

                // 调用仓库的远程服务锁定库存，所有的商品都需要锁库存
                WareLockVo wareLockVo = new WareLockVo();
                wareLockVo.setOrderSn(orderTo.getOrder().getOrderSn());
                List<WareLockVo.WareOrderItem> wareOrderItems = orderTo.getOrderItems().stream().map(item -> {
                    WareLockVo.WareOrderItem wareOrderItem = new WareLockVo.WareOrderItem();
                    wareOrderItem.setSkuId(item.getSkuId());
                    wareOrderItem.setCount(item.getSkuQuantity());
                    wareOrderItem.setSkuName(item.getSkuName());
                    return wareOrderItem;
                }).collect(Collectors.toList());
                wareLockVo.setOrderItems(wareOrderItems);

                R lockInfo = myMallWareFeignService.lockStock(wareLockVo);
                if (lockInfo.getCode() == 0) {
                    // 扣减积分
                    kafkaProducer.send(KafkaConsumer.lockedTopic, orderTo.getOrder().getOrderSn(), orderTo.getOrder());
                }else {
                    // 库存不足
                    orderSubmitRespVo.setCode(3);
                    throw new RuntimeException("库存不足");
                }


                // 调用其他服务

            }else {
                //如果验价失败
                orderSubmitRespVo.setCode(2);
            }

        }



        return orderSubmitRespVo;
    }

    @Override
    public OrderEntity getOrderBySn(String orderSn) {
        return this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn" , orderSn));
    }

    @Override
    public String payOrder(String orderSn) {
        // 通过orderSn查询一系列的订单信息
        PayVo payVo = new PayVo();

        payVo.setOut_trade_no(orderSn);

        OrderEntity orderBySn = this.getOrderBySn(orderSn);

        BigDecimal totalAmount = orderBySn.getTotalAmount();
        BigDecimal realTotalAmount = totalAmount.setScale(2, BigDecimal.ROUND_HALF_UP);
        payVo.setTotal_amount(realTotalAmount.toString());

        payVo.setSubject(orderBySn.getMemberUsername());
        payVo.setBody(orderBySn.getMemberUsername());

        return aliPayTemplate.pay(payVo);
    }

    private void saveOrder(OrderTo orderTo) {
        this.save(orderTo.getOrder());

        List<OrderItemEntity> orderItems = orderTo.getOrderItems();

        // 批量保存订单项,上一步订单保存成功会生成一个orderId，将orderid写入订单项的orderid中
        List<OrderItemEntity> saveList = orderItems.stream().map(item -> {
                    item.setOrderId(orderTo.getOrder().getId());
                    return item;
                }
        ).collect(Collectors.toList());
        this.orderItemService.saveBatch(saveList);
    }

    private OrderTo createOrderTo(OrderSubmitVo orderSubmitVo) {

        // 创建订单
        // 生成订单号

            // 创建订单项
                // 订单号
                // spu
                // sku
                // jifenxinxi
                // 优惠信息

        // 获取收货地址
        // 获取运费
        // 创建订单

        // 验价

        // 创建订单
        OrderTo orderTo = new OrderTo();
        OrderEntity orderEntity = buildOrder(orderSubmitVo, orderTo);
        orderTo.setOrder(orderEntity);


        // 对每个商品创建订单项
        List<OrderItemEntity> orderItems = createOrderItems(orderEntity.getOrderSn());
        orderTo.setOrderItems(orderItems);


        // 传入订单 、订单项 计算价格、积分、成长值等相关信息
        computerPrice(orderEntity,orderItems, orderTo);


        return orderTo;
    }

    /**
     * 根据每个订单项目的优惠、积分等计算当前总订单的积分，成长值等信息
     * @param orderEntity
     * @param orderItems
     */
    private void computerPrice(OrderEntity orderEntity, List<OrderItemEntity> orderItems, OrderTo orderTo) {

        BigDecimal coupon = new BigDecimal("0.0");
        Integer gift = 0;
        Integer giftGrowth = 0;
        BigDecimal integration = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");
        BigDecimal realAmount = new BigDecimal("0.0");


        for (OrderItemEntity item : orderItems) {
            coupon  = coupon.add(item.getCouponAmount());

            // 积分优惠
            integration = integration.add(item.getIntegrationAmount());
            // 打折金额
            promotion = promotion.add(item.getPromotionAmount());


            gift += item.getGiftIntegration();
            giftGrowth += item.getGiftGrowth();
            realAmount = realAmount.add(item.getRealAmount());
        }

        // 订单总额
        orderEntity.setTotalAmount(realAmount);
        // 应付总额
        orderEntity.setPayAmount(realAmount.add(orderEntity.getFreightAmount()));
        orderTo.setPayPrice(orderEntity.getPayAmount());

        // 优惠信息
        orderEntity.setDiscountAmount(promotion);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setCouponAmount(coupon);

        // 积分成长值信息
        orderEntity.setIntegration(giftGrowth);
        orderEntity.setGrowth(giftGrowth);


    }

    public OrderEntity buildOrder(OrderSubmitVo orderSubmitVo, OrderTo orderTo) {
        // 创建订单项
        OrderEntity orderEntity = new OrderEntity();

        // 订单号获取ordersn
        String orderSn = IdWorker.getTimeId();
        orderEntity.setOrderSn(orderSn);

        // 根据addrid获取收获地址
        R info = myMallWareFeignService.fare(orderSubmitVo.getAddrId());
        FareVo fareVo = info.get(new TypeReference<FareVo>(){});
        // 设置运费
        orderTo.setFare(fareVo.getPayPrice());
        orderEntity.setFreightAmount(fareVo.getPayPrice());
        // 设置收货人的信息

        orderEntity.setReceiverCity(fareVo.getAddress().getCity());
        orderEntity.setReceiverDetailAddress(fareVo.getAddress().getDetailAddress());
        orderEntity.setReceiverName(fareVo.getAddress().getName());
        orderEntity.setReceiverPhone(fareVo.getAddress().getPhone());
        orderEntity.setReceiverPostCode(fareVo.getAddress().getPostCode());
        orderEntity.setReceiverProvince(fareVo.getAddress().getProvince());
        orderEntity.setReceiverRegion(fareVo.getAddress().getRegion());


        // 计算订单的实际价格

        // 填充订单的一些属性
        orderEntity.setModifyTime(new Date());

        orderEntity.setCreateTime(new Date());
        orderEntity.setCommentTime(new Date());
        orderEntity.setReceiveTime(new Date());
        orderEntity.setDeliveryTime(new Date());


        MemberEntityVo rsepVo = OrderInteceptor.loginUser.get();
        orderEntity.setMemberId(rsepVo.getId());
        orderEntity.setMemberUsername(rsepVo.getUsername());
        orderEntity.setBillReceiverEmail(rsepVo.getEmail());

        // 设置订单状态
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setDeleteStatus(OrderStatusEnum.CREATE_NEW.getCode());

        orderEntity.setAutoConfirmDay(7);


        return orderEntity;

    }
    private List<OrderItemEntity> createOrderItems(String orderSn) {
        // 创建订单项
        // 订单号
        // spu
        // sku
        // jifenxinxi
        // 优惠信息

        //查询购物车中的选中的数据项，feign的拦截器会传输当前用户的session过去
        List<OrderItemVo> checkedItems = myMallCartFeignService.getCheckedItems();
        List<OrderItemEntity> collect = checkedItems.stream().map(item -> {
            OrderItemEntity orderItemEntity = new OrderItemEntity();
            orderItemEntity.setOrderSn(orderSn);

            // 设置sku的信息，在购物车中就有
            orderItemEntity.setSkuId(item.getSkuId());
            orderItemEntity.setSkuName(item.getTitle());
            orderItemEntity.setSkuPic(item.getImage());
            orderItemEntity.setSkuPrice(item.getPrice());
            // 把一个集合按照指定的字符串进行分割得到一个字符串
            String skuAttr = StringUtils.collectionToDelimitedString(item.getSkuAttr(), ";");
            orderItemEntity.setSkuAttrsVals(skuAttr);
            orderItemEntity.setSkuQuantity(item.getCount());

            // 根据skuid获取spu信息
            R r = myMallProductFeignService.spuinfoBysku(item.getSkuId());
            SpuVo spuVo = r.get(new TypeReference<SpuVo>() {});

            orderItemEntity.setSpuBrand(spuVo.getBrandId()+"");
            orderItemEntity.setSpuId(spuVo.getId());
            orderItemEntity.setSpuName(spuVo.getSpuName());
            orderItemEntity.setCategoryId(spuVo.getCatalogId());


            // 4.积分信息 买的数量越多积分越多 成长值越多
            orderItemEntity.setGiftGrowth(item.getPrice().multiply(new BigDecimal(item.getCount())).intValue());
            orderItemEntity.setGiftIntegration(item.getPrice().multiply(new BigDecimal(item.getCount())).intValue());


            // 5.订单项的价格信息 优惠金额
            orderItemEntity.setPromotionAmount(new BigDecimal("0.0"));
            orderItemEntity.setCouponAmount(new BigDecimal("0.0"));
            orderItemEntity.setIntegrationAmount(new BigDecimal("0.0"));


            // 当前订单项的实际金额
            BigDecimal orign = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity().toString()));
            // 减去各种优惠的价格
            BigDecimal subtract = orign.subtract(orderItemEntity.getCouponAmount())
                    .subtract(orderItemEntity.getPromotionAmount())
                    .subtract(orderItemEntity.getIntegrationAmount());
            orderItemEntity.setRealAmount(subtract);
            return orderItemEntity;
        }).collect(Collectors.toList());
        return collect;
    }

}