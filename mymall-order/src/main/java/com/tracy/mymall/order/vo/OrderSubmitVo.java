package com.tracy.mymall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单提交业务不需要用户提交商品信息，直接去购物车查询选中的数据，可以让页面把确认页的价格传过来，用来做确认页价格和购物车选中数据的对比
 */
@Data
public class OrderSubmitVo {
    private Long addrId;
    private BigDecimal payPrice;
    private String orderToken;
}
