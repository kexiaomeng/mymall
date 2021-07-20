package com.tracy.mymall.order.vo;

import com.tracy.mymall.common.vo.MemberReceiveAddressVo;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FareVo {
    private MemberReceiveAddressVo address;
    // 运费信息
    private BigDecimal payPrice;
}
