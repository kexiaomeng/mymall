package com.tracy.mymall.common.dto.mq;

import lombok.Data;

@Data
public class WareLockedDto {

    /**
     * 任务项id
     */
    private Long taskId;

    private StockDetailTo stockDetailTo;
}
