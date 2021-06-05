package com.tracy.mymall.search.vo;

import com.tracy.mymall.common.dto.es.SkuEsDto;
import lombok.Data;

import java.util.List;

@Data
public class SearchResult {
    // 查询到的es中所有商品的信息
    private List<SkuEsDto> product;

    // 以下是分页信息
    // 当前页数
    private Integer pageNum = 0;
    // 总数
    private Long total;
    // 总页数
    private Integer totalPages;


    // 以下是分类、品牌等信息
    private List<BrandVo> brands;
    // 页面展示的分类信息// 有多个分类可选
    private List<CatalogVo> catalogs;
    // 页面展示的属性信息
    private List<AttrVo> attrs;
    // 所有的页数集合
    private List<Integer> pageNavs;

    // 面包屑导航
    private List<NavVo> navs;

    @Data
    public static class NavVo{
        private String navName;
        private String navValue;
        // 点击面包屑后跳转的地址，点击后去除当前的条件
        private String link;
    }

    @Data
    public static class BrandVo{
        private Long brandId;

        private String brandName;

        private String brandImg;

    }

    @Data
    public static class CatalogVo{
        private Long catalogId;
        private String catalogName;

    }

    @Data
    public static class AttrVo{
        // 属性ID
        private Long attrId;
        // 属性名称
        private String attrName;
        //属性值
        private List<String> attrValue;


    }
}
