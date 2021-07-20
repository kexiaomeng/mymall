package com.tracy.mymall.order.vo;

import com.tracy.mymall.order.entity.OrderEntity;
import lombok.Data;

@Data
public class OrderSubmitRespVo {
    private OrderEntity order;
    /**
     * code为0表示成功
     */
    private Integer code;
}
