package com.tracy.mymall.product.vo;

import com.tracy.mymall.product.entity.SkuImagesEntity;
import com.tracy.mymall.product.entity.SkuInfoEntity;
import com.tracy.mymall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

@Data
public class SkuItemVo {
    // 1. 查询sku基本信息
    private SkuInfoEntity info;
    // 2. 查询sku销售属性,销售属性是一堆列表,其中销售属性是需要查询的sku对应的spu下的所有sku的属性
    // 切换时根据每个销售属性找出包含当前销售属性值的sku，选定多个销售属性时，将sku作交集查询具体的sku
    private List<SkuItemAttrVo> saleAttr;

    // 3. 查询商品介绍，spu描述信息
    private SpuInfoDescEntity desc;
    // 4. 查询商品规格信息,规格属性展示时按照属性分组展示
    private List<SpuItemGroupAttrVo> groupAttrs;

    //2、sku的图片信息    pms_sku_images
    private List<SkuImagesEntity> images;

    private Object seckillSkuVo = null;
    private Boolean hasStock = true;





}
