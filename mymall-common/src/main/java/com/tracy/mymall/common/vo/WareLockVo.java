package com.tracy.mymall.common.vo;

import lombok.Data;

import java.util.List;

@Data
public class WareLockVo {
    private String orderSn;

    private List<WareOrderItem> orderItems;



    @Data
    public static class WareOrderItem{
        private Long skuId;
        private Integer count;
        private String skuName;
    }
}
