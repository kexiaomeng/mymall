package com.tracy.mymall.order.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class OrderConfirmVo {


    // 折扣列表
    @Setter
    @Getter
    private BigDecimal discount;
    /**
     * 收获地址
     */
    @Setter
    @Getter
    private List<MemberAddressVo> address;

    /**
     * 所有选中的购物项
     */
    @Setter @Getter
    private List<OrderItemVo> items;

    /**
     * 积分信息
     */
    @Setter @Getter
    private Integer integration;

    /**
     * 防重令牌
     */
    @Setter @Getter
    private String orderToken;

    private BigDecimal total;
    private BigDecimal payPrice;
    private Integer count;

    @Setter @Getter
    Map<Long,Boolean> stocks;

    /**
     * 获取商品总价格
     */
    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal("0");
        if(items!= null){
            for (OrderItemVo item : items) {
                sum = sum.add(item.getPrice().multiply(new BigDecimal(item.getCount().toString())));
            }
        }
        return sum;
    }

    /**
     * 应付的价格
     */
    public BigDecimal getPayPrice() {
        return getTotal();
    }

    public Integer getCount(){
        Integer i = 0;
        if(items!= null){
            for (OrderItemVo item : items) {
                i += item.getCount();
            }
        }
        return i;
    }
    /**
     * 发票信息...
     */
}
