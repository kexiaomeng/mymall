package com.tracy.mymall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.tracy.mymall.common.constant.ProductConst;
import com.tracy.mymall.common.dto.SkuHasStockDto;
import com.tracy.mymall.common.dto.SkuReductionDto;
import com.tracy.mymall.common.dto.SpuBoundsDto;
import com.tracy.mymall.common.dto.es.SkuEsDto;
import com.tracy.mymall.common.utils.R;
import com.tracy.mymall.product.entity.*;
import com.tracy.mymall.product.feign.CouponFeignService;
import com.tracy.mymall.product.feign.ElasticSearchService;
import com.tracy.mymall.product.feign.WareFeignService;
import com.tracy.mymall.product.service.*;
import com.tracy.mymall.product.vo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
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
    private BrandService brandService;
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private WareFeignService wareFeignService;
    @Autowired
    private ElasticSearchService elasticSearchService;
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
        // 1. ??????spu???????????? :pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuSaveVo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.save(spuInfoEntity);
        // 2. ??????spu????????????: pms_spu_images
        List<String> images = spuSaveVo.getImages();
        List<SpuImagesEntity> spuImagesEntities = images.stream().map(image -> {
            SpuImagesEntity spuImagesEntity = new SpuImagesEntity();
            spuImagesEntity.setSpuId(spuInfoEntity.getId());
            spuImagesEntity.setImgUrl(image);
            return spuImagesEntity;
        }).collect(Collectors.toList());
        spuImagesService.saveBatch(spuImagesEntities);

        // 3. ??????spu????????????:pms_spu_info_desc
        List<String> decript = spuSaveVo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(StringUtils.join(decript, ","));
        spuInfoDescService.save(spuInfoDescEntity);
        // 4. ??????spu???????????????:pms_product_attr_value

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
        // 5. ???????????????????????????spu???????????????: sms-> sms_spu_bounds
        SpuBoundsDto spuBoundsDto = new SpuBoundsDto();
        Bounds bounds = spuSaveVo.getBounds();
        BeanUtils.copyProperties(bounds, spuBoundsDto);
        spuBoundsDto.setSpuId(spuInfoEntity.getId());
        couponFeignService.saveSpuBounds(spuBoundsDto);

        // 6. ????????????spu?????????sku??????
        List<Skus> skus = spuSaveVo.getSkus();
        if (skus.isEmpty()) {
            return;
        }
        // 6.1 sku???????????????:pms_sku_info
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
            // 6.2 sku??????????????????pms_sku_images

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
            // 6.3:sku????????????????????????pms_sku_sale_attr_value

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

            // 6.4 sku?????????????????????:sms_sku_ladder\sms_sku_full_reduction\sms_member_price
            SkuReductionDto skuReductionTo = new SkuReductionDto();
            BeanUtils.copyProperties(sku, skuReductionTo);
            skuReductionTo.setSkuId(skuInfoEntity.getSkuId());
            if (skuReductionTo.getFullCount() >0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) > 0) {
                R r = couponFeignService.saveSkuReductionTo(skuReductionTo);

            }


        });



    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> spuInfoEntityQueryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            spuInfoEntityQueryWrapper.and((wrapper)->{

                wrapper.eq("id", params.get("key")).or().eq("spu_name", params.get("key"));
            });
        }
        String catelogId = (String) params.get("catelogId");

        if (StringUtils.isNotEmpty(catelogId)) {
            spuInfoEntityQueryWrapper.and((wrapper)->{

                wrapper.eq("catalog_id", params.get("catelogId"));
            });
        }
        String brandId = (String) params.get("brandId");
        if (StringUtils.isNotEmpty(brandId)) {
            spuInfoEntityQueryWrapper.and((wrapper)->{

                wrapper.eq("brand_id", params.get("brandId"));
            });
        }
        String status = (String) params.get("status");

        if (StringUtils.isNotEmpty(status)) {
            spuInfoEntityQueryWrapper.and((wrapper)->{

                wrapper.eq("publish_status", params.get("status"));
            });
        }
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                spuInfoEntityQueryWrapper
        );

        return new PageUtils(page);
    }

    /**
     * ????????????????????????????????????????????????es???
     * @param spuId
     */
    @Override
    public void spuUp(Long spuId) {
        // 1???spu???????????????SKU

        // ?????????????????????attrs??????spu????????????????????????sku???atrs?????????????????????????????????
        // ?????????????????????attr??????
        // 1. ???????????????spu???????????????attr
        List<ProductAttrValueEntity> attrValueEntities = productAttrValueService.baseAttrListforspu(spuId);
        // ????????????????????????
        List<Long> attrIds = attrValueEntities.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());
//        List<AttrEntity> attrEntities = (List<AttrEntity>) attrService.listByIds(attrIds);
//        List<Long> searchAttrIds = attrEntities.stream().filter(item -> {
//            return item.getSearchType() == 1;
//        }).map(AttrEntity::getAttrId).collect(Collectors.toList());
        List<Long> searchIds = attrService.selectSearchIds(attrIds);
        List<SkuEsDto.Attrs> attrsEsList = attrValueEntities.stream().filter(item -> searchIds.contains(item.getAttrId())).map(item -> {
            SkuEsDto.Attrs attrs = new SkuEsDto.Attrs();
            BeanUtils.copyProperties(item, attrs);
            return attrs;
        }).collect(Collectors.toList());

        // ???????????????sku
        List<SkuInfoEntity> skuInfoEntities = skuInfoService.getSkuBySpuId(spuId);
        //  ???????????????????????????????????????
        List<Long> skuIds = skuInfoEntities.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
        Map<Long, Boolean> hasStockMap = null;
        boolean stockSuccess = true;
        try {
            R r = wareFeignService.hasStock(skuIds);
            String json = JSON.toJSONString(r.get("data"));
            List<SkuHasStockDto> skuHasStockDtos = JSON.parseObject(json, new TypeReference<ArrayList<SkuHasStockDto>>(){});
            // ??????????????????map
            hasStockMap = skuHasStockDtos.stream().collect(Collectors.toMap(SkuHasStockDto::getSkuId, SkuHasStockDto::getHasStock));
        }catch (Exception e) {
            log.error("??????????????????????????????", e);
            stockSuccess = false;
        }


        // ????????????sku????????????es??????????????????
        boolean finalStockSuccess = stockSuccess;
        Map<Long, Boolean> finalHasStockMap = hasStockMap;
        List<SkuEsDto> collect = skuInfoEntities.stream().map(skuInfoEntity -> {
            SkuEsDto skuEsDto = new SkuEsDto();
            // ???????????????????????????
            BeanUtils.copyProperties(skuInfoEntity, skuEsDto);
            // ???????????????????????????skuPrice,skuImg,hotScore, hasstock???catalogName,brandName,
            skuEsDto.setSkuPrice(skuInfoEntity.getPrice());
            skuEsDto.setSkuImg(skuInfoEntity.getSkuDefaultImg());
            // ??????????????????0
            skuEsDto.setHotScore(0L);
            skuEsDto.setHasStock(finalStockSuccess ? finalHasStockMap.get(skuInfoEntity.getSkuId()) : false);
            // ??????catelogname???brandname
            BrandEntity brandEntity = brandService.getById(skuInfoEntity.getBrandId());
            skuEsDto.setBrandName(brandEntity.getName());
            skuEsDto.setBrandImg(brandEntity.getLogo());
            CategoryEntity categoryEntity = categoryService.getById(skuInfoEntity.getCatalogId());
            skuEsDto.setCatalogName(categoryEntity.getName());
            // ????????????
            skuEsDto.setAttrs(attrsEsList);


            return skuEsDto;
        }).collect(Collectors.toList());

        // ?????????????????????ElasticSearchService???es??????

        R r = elasticSearchService.productStatusUp(collect);

        // ??????????????????????????????????????????????????????
        if (0 == r.getCode()) {
            SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
            spuInfoEntity.setId(spuId);
            spuInfoEntity.setPublishStatus(ProductConst.PublishStatusEnum.PRODUCT_UP.getType());
            this.baseMapper.update(spuInfoEntity, new UpdateWrapper<SpuInfoEntity>().eq("id", spuId));
        }else {
            // TODO ??????????????????????????????,????????????
            log.error("?????????????????????es?????????");
        }

    }

}