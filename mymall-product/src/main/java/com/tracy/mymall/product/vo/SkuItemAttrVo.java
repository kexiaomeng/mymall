package com.tracy.mymall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;


@ToString
@Data
public class SkuItemAttrVo {
    private long attrId;
    private String attrName;
    // 某个属性下会有多种值，例如颜色下有：白色、黄色、红色等
    private List<AttrValueWithSkuVo> attrValues;
}
