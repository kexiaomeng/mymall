package com.tracy.mymall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * 商品服务传过来的查询条件
 * keyword=小米&
 * sort=saleCount_desc/asc&
 * hasStock=0/1&
 * skuPrice=400_1900&
 * brandId=1&
 * catalog3Id=1&
 * attrs=1_3G:4G:5G&
 * attrs=2_骁龙845&
 * attrs=4_高清屏
 */
@Data
public class SearchParam {
    // 关键字
    private String keyword;
    // 分类id
    private Long catalog3Id;
    /**
     * 排序条件
     * sort=saleCount_desc/asccatalogLoader
     * sort=salePrice_desc/asc
     */
    private String sort;
    /**
     * 一堆过滤条件
     */
    // 是否有库存
    private Integer hasStock = 1;
    //价格区间 price=_500  || 1_500 || 500_
    private String skuPrice;
    // 品牌，可以多选
    private List<Long> brandId;
    // 属性，可以多选，用:分割每个属性的多个值
    private List<String> attrs;
    // 分页
    private Integer pageNum = 1;
    // 查询参数
    private String queryString;
    // url
    private String url;
}
