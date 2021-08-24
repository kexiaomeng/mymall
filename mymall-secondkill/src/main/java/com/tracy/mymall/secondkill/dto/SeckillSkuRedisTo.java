package com.tracy.mymall.secondkill.dto;

import com.tracy.mymall.common.dto.SkuEntityDto;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeckillSkuRedisTo {
    private Long id;
    /**
     * 活动id
     */
    private Long promotionId;
    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;
    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 秒杀总量
     */
    private Integer seckillCount;
    /**
     * 每人限购数量
     */
    private Integer seckillLimit;
    /**
     * 排序
     */
    private Integer seckillSort;

    private SkuEntityDto skuInfoVo;

    private Long startTime;
    private Long endTime;

    /**
     * 商品秒杀随机码，用来防止恶意请求，在秒杀正式开始才暴露出去
     */
    private String randomCode;
}
