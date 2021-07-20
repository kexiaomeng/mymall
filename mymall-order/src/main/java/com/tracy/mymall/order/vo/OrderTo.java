package com.tracy.mymall.order.vo;

import com.tracy.mymall.order.entity.OrderEntity;
import com.tracy.mymall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单信息
 */
@Data
public class OrderTo {
    private OrderEntity order;

    private List<OrderItemEntity> orderItems;

    /**
     * 订单计算的应付价格
     */
    private BigDecimal payPrice;

    /**
     * 运费
     */
    private BigDecimal fare;
}
