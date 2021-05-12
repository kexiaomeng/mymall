package com.tracy.mymall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CateLogIndexVo {

    private Long catalog1Id;
    private Long id;
    private String name;
    private List<CateLog3Vo> catalog3List;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CateLog3Vo{
        private Long catalog2Id;
        private Long id;
        private String name;
    }

}

