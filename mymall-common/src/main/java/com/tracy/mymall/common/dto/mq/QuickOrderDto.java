package com.tracy.mymall.common.dto.mq;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class QuickOrderDto {


    /**
     * member_id
     */
    private Long memberId;
    /**
     * 订单号
     */
    private String orderSn;

    /**
     * create_time
     */
    private Date createTime;
    /**
     * 用户名
     */
    private String memberUsername;

    /**
     * 应付总额
     */
    private BigDecimal payAmount;
    private Integer num;
    private Long skuId;
    private BigDecimal seckillPrice;

    private Long promotionSessionId;

}
