package com.tracy.mymall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.tracy.mymall.common.utils.R;
import com.tracy.mymall.product.entity.SkuImagesEntity;
import com.tracy.mymall.product.entity.SpuInfoDescEntity;
import com.tracy.mymall.product.entity.SpuInfoEntity;
import com.tracy.mymall.product.feign.MymallSeckillFeignService;
import com.tracy.mymall.product.service.*;
import com.tracy.mymall.product.vo.SeckillSkuRedisTo;
import com.tracy.mymall.product.vo.SkuItemAttrVo;
import com.tracy.mymall.product.vo.SkuItemVo;
import com.tracy.mymall.product.vo.SpuItemGroupAttrVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tracy.mymall.common.utils.PageUtils;
import com.tracy.mymall.common.utils.Query;

import com.tracy.mymall.product.dao.SkuInfoDao;
import com.tracy.mymall.product.entity.SkuInfoEntity;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private SpuInfoService spuInfoService;
    @Autowired
    private MymallSeckillFeignService seckillFeignService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }


    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(wrpper->{
                wrpper.eq("sku_id", key).or().like("sku_name", key);
            });
        }

        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equals(catelogId)) {
            queryWrapper.eq("catalog_id", catelogId);
        }

        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equals(brandId)) {
            queryWrapper.eq("brand_id", brandId);
        }

        String min = (String) params.get("min");
        if (!StringUtils.isEmpty(min)) {
            BigDecimal minDecimal = new BigDecimal(min);
            if (minDecimal.compareTo(new BigDecimal(0))==1) {
                queryWrapper.ge("price", min);
            }
        }

        String max = (String) params.get("max");
        if (!StringUtils.isEmpty(max)) {
            BigDecimal maxDecimal = new BigDecimal(max);
            if (maxDecimal.compareTo(new BigDecimal(0))==1) {
                queryWrapper.le("price", maxDecimal);

            }
        }

        IPage<SkuInfoEntity> page = this.page(new Query<SkuInfoEntity>().getPage(params), queryWrapper);
        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkuBySpuId(Long spuId) {
        return this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
    }

    /**
     * ???????????????????????????????????????,???????????????????????????
     * @param skuId
     * @return
     */
    @Override
    public SkuItemVo getSkuItem(Long skuId) {
        SkuItemVo skuItemVo = new SkuItemVo();
        // 1. ??????sku????????????
        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfoEntity skuInfoEntity = this.getById(skuId);
            skuItemVo.setInfo(skuInfoEntity);
            return skuInfoEntity;
        }, threadPoolExecutor);




        CompletableFuture<Void> skuSaleAttrFuture = infoFuture.thenAcceptAsync(skuinfo -> {
            // 2. ??????sku????????????,??????sku?????????spu????????????sku????????????????????????????????????
            List<SkuItemAttrVo> skuItemAttrVos = skuSaleAttrValueService.getAllSkuSaleAttrBySpuId(skuinfo.getSpuId());

            skuItemVo.setSaleAttr(skuItemAttrVos);
        }, threadPoolExecutor);


        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync(skuinfo -> {
            // 3. ?????????????????????spu????????????
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(skuinfo.getSpuId());
            skuItemVo.setDesc(spuInfoDescEntity);
        }, threadPoolExecutor);


        CompletableFuture<Void> spuAttrFuture = infoFuture.thenAcceptAsync(skuinfo -> {
            // 4. ????????????????????????,??????????????????????????????
            List<SpuItemGroupAttrVo> groupAttrVos = attrGroupService.getAttrGroupAttrBySpuId(skuinfo.getSpuId(), skuinfo.getCatalogId());
            skuItemVo.setGroupAttrs(groupAttrVos);
        }, threadPoolExecutor);


        CompletableFuture<Void> imgFuture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> skuImagesEntities = skuImagesService.queryBySkuId(skuId);
            skuItemVo.setImages(skuImagesEntities);
        }, threadPoolExecutor);

        // ???????????????????????????
        CompletableFuture<Void> seckillFuture = CompletableFuture.runAsync(() -> {
            R msg = seckillFeignService.getSkuSecondkillMsg(skuId);
            if (msg.getCode() == 0) {
                SeckillSkuRedisTo seckillSkuRedisTo = msg.get(new TypeReference<SeckillSkuRedisTo>() {
                });
                skuItemVo.setSeckillSkuVo(seckillSkuRedisTo);
            }
        }, threadPoolExecutor);

        // ????????????????????????
        try {
            CompletableFuture.allOf(imgFuture, spuAttrFuture, descFuture, skuSaleAttrFuture, seckillFuture).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        return skuItemVo;
    }

    @Override
    public SpuInfoEntity getSpuBySkuId(Long skuId) {
        SkuInfoEntity byId = this.getById(skuId);
        Long spuId = byId.getSpuId();
        SpuInfoEntity spuInfoEntity = spuInfoService.getById(spuId);
        return spuInfoEntity;
    }


}