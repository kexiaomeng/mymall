package com.tracy.mymall.search.service;

import com.tracy.mymall.common.dto.es.SkuEsDto;

import java.io.IOException;
import java.util.List;

public interface ProductSaveService {
    boolean productStatusUp(List<SkuEsDto> skuEsDtos) throws IOException;
}
