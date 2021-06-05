package com.tracy.mymall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;
@Data
@ToString
public class SpuItemGroupAttrVo {
    private String groupName;
    private List<SpuItemAttrVo> attrs;
}
