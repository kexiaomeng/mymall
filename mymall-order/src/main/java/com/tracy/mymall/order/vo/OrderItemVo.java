package com.tracy.mymall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderItemVo {
    private Long skuId;
    /**
     * 是否被选中
     */
    private Boolean check;
    /**
     * sku属性
     */
    private List<String> skuAttr;
    private String title;
    private String image;

    private Integer count;

    private BigDecimal price;
    /**
     * totalPrice由count和price共同计算出来
     */
    private BigDecimal totalPrice;
    private BigDecimal weight = new BigDecimal("1.0");
}
