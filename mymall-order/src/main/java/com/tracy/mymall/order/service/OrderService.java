package com.tracy.mymall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tracy.mymall.common.utils.PageUtils;
import com.tracy.mymall.order.entity.OrderEntity;
import com.tracy.mymall.order.vo.OrderConfirmVo;
import com.tracy.mymall.order.vo.OrderSubmitRespVo;
import com.tracy.mymall.order.vo.OrderSubmitVo;

import java.util.Map;

/**
 * 订单
 *
 * @author kexiaomeng
 * @email kexiaomeng@foxmail.com
 * @date 2021-04-22 01:31:12
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo confirmOrder();

    OrderSubmitRespVo submitOrder(OrderSubmitVo orderSubmitVo);

    OrderEntity getOrderBySn(String orderSn);

    String payOrder(String orderSn);
}

