package com.tracy.mymall.product.feign;

import com.tracy.mymall.common.dto.es.SkuEsDto;
import com.tracy.mymall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("mymall-search")
public interface ElasticSearchService {
    @PostMapping("/search/save/product")
    R productStatusUp(@RequestBody List<SkuEsDto> skuEsDtos);
}
