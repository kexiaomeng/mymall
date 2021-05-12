package com.tracy.mymall.common.dto;

import lombok.Data;

@Data
public class SkuHasStockDto {
    private Long skuId;
    private Boolean hasStock;
}
