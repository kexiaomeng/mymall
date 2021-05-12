package com.tracy.mymall.search.conroller;

import com.tracy.mymall.common.dto.es.SkuEsDto;
import com.tracy.mymall.common.exception.ExceptionEnum;
import com.tracy.mymall.common.utils.R;
import com.tracy.mymall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/search/save")
public class ElasticSaveController {
    @Autowired
    private ProductSaveService productSaveService;
    /**
     * 商品上架，将信息保存到es中
     */
    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsDto> skuEsDtos) {
        boolean b = false;
        try {
            b = productSaveService.productStatusUp(skuEsDtos);
        }catch(Exception e) {
            log.error("", e);
            return R.error(ExceptionEnum.PRODUCT_ES_STATUS_UP.getErrorCode(), ExceptionEnum.PRODUCT_ES_STATUS_UP.getDesc());
        }
        return b ? R.ok() : R.error(ExceptionEnum.PRODUCT_ES_STATUS_UP.getErrorCode(), ExceptionEnum.PRODUCT_ES_STATUS_UP.getDesc());


    }
}
