package com.tracy.mymall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;


@Data
public class FareVo {
    private MemberReceiveAddress address;
    // 运费信息
    private BigDecimal payPrice;
}
