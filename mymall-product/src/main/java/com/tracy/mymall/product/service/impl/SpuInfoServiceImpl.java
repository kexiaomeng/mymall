package com.tracy.mymall.product.service.impl;

import com.tracy.mymall.common.dto.SkuReductionDto;
import com.tracy.mymall.common.dto.SpuBoundsDto;
import com.tracy.mymall.common.utils.R;
import com.tracy.mymall.product.entity.*;
import com.tracy.mymall.product.feign.CouponFeignService;
import com.tracy.mymall.product.service.*;
import com.tracy.mymall.product.vo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tracy.mymall.common.utils.PageUtils;
import com.tracy.mymall.common.utils.Query;

import com.tracy.mymall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuImagesService spuImagesService;

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private ProductAttrValueService productAttrValueService;
    @Autowired
    private AttrService attrService;

    @Autowired
    private SkuInfoService skuInfoService;
    @Autowired
    private SkuImagesService skuImagesService;
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private CouponFeignService couponFeignService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSpuInfo(SpuSaveVo spuSaveVo) {
        // 1. 保存spu基本信息 :pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuSaveVo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.save(spuInfoEntity);
        // 2. 保存spu图片信息: pms_spu_images
        List<String> images = spuSaveVo.getImages();
        List<SpuImagesEntity> spuImagesEntities = images.stream().map(image -> {
            SpuImagesEntity spuImagesEntity = new SpuImagesEntity();
            spuImagesEntity.setSpuId(spuInfoEntity.getId());
            spuImagesEntity.setImgUrl(image);
            return spuImagesEntity;
        }).collect(Collectors.toList());
        spuImagesService.saveBatch(spuImagesEntities);

        // 3. 保存spu描述信息:pms_spu_info_desc
        List<String> decript = spuSaveVo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(StringUtils.join(decript, ","));
        spuInfoDescService.save(spuInfoDescEntity);
        // 4. 保存spu的规格参数:pms_product_attr_value

        List<BaseAttrs> baseAttrs = spuSaveVo.getBaseAttrs();
        List<ProductAttrValueEntity> productAttrValueEntities = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            productAttrValueEntity.setAttrId(attr.getAttrId());
            AttrEntity byId = attrService.getById(attr.getAttrId());
            productAttrValueEntity.setAttrName(byId.getAttrName());
            productAttrValueEntity.setAttrValue(attr.getAttrValues());
            productAttrValueEntity.setQuickShow(attr.getShowDesc());
            productAttrValueEntity.setSpuId(spuInfoEntity.getId());
            return productAttrValueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveBatch(productAttrValueEntities);
        // 5. 调用远程服务，保存spu的积分信息: sms-> sms_spu_bounds
        SpuBoundsDto spuBoundsDto = new SpuBoundsDto();
        Bounds bounds = spuSaveVo.getBounds();
        BeanUtils.copyProperties(bounds, spuBoundsDto);
        spuBoundsDto.setSpuId(spuInfoEntity.getId());
        couponFeignService.saveSpuBounds(spuBoundsDto);

        // 6. 保存当前spu对应的sku信息
        List<Skus> skus = spuSaveVo.getSkus();
        if (skus.isEmpty()) {
            return;
        }
        // 6.1 sku的基本信息:pms_sku_info
        skus.forEach(sku -> {


            SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
            BeanUtils.copyProperties(sku, skuInfoEntity);
            Images defaultImage = null;
            List<Images> skuImages = sku.getImages();
            for (Images skuImage : skuImages) {
                if (skuImage.getDefaultImg() == 1) {
                    defaultImage = skuImage;
                    break;
                }
            }
            skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
            skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
            skuInfoEntity.setSpuId(spuInfoEntity.getId());
            skuInfoEntity.setSkuDefaultImg(defaultImage.getImgUrl());
            skuInfoEntity.setSaleCount(0L);
            skuInfoService.save(skuInfoEntity);
            // 6.2 sku的图片信息：pms_sku_images

            Images finalDefaultImage = defaultImage;
            List<SkuImagesEntity> skuImagesEntities = skuImages.stream().map(skuImag -> {
                SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                skuImagesEntity.setSkuId(skuInfoEntity.getSkuId());
                skuImagesEntity.setDefaultImg(finalDefaultImage.getDefaultImg());
                skuImagesEntity.setImgUrl(skuImag.getImgUrl());

                return skuImagesEntity;
            }).filter(entity ->
                StringUtils.isNotEmpty(entity.getImgUrl())
            ).collect(Collectors.toList());
            skuImagesService.saveBatch(skuImagesEntities);
            // 6.3:sku的销售属性信息：pms_sku_sale_attr_value

            List<Attr> attr = sku.getAttr();
            List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(cAttr -> {
                SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                skuSaleAttrValueEntity.setAttrId(cAttr.getAttrId());
                skuSaleAttrValueEntity.setAttrName(cAttr.getAttrName());
                skuSaleAttrValueEntity.setAttrValue(cAttr.getAttrValue());
                skuSaleAttrValueEntity.setSkuId(skuInfoEntity.getSkuId());
                return skuSaleAttrValueEntity;
            }).collect(Collectors.toList());
            skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

            // 6.4 sku的优惠满减信息:sms_sku_ladder\sms_sku_full_reduction\sms_member_price
            SkuReductionDto skuReductionTo = new SkuReductionDto();
            BeanUtils.copyProperties(sku, skuReductionTo);
            skuReductionTo.setSkuId(skuInfoEntity.getSkuId());
            if (skuReductionTo.getFullCount() >0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) > 0) {
                R r = couponFeignService.saveSkuReductionTo(skuReductionTo);

            }


        });



    }

}