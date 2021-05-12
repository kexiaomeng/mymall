package com.tracy.mymall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.tracy.mymall.common.constant.ProductConst;
import com.tracy.mymall.common.dto.es.SkuEsDto;
import com.tracy.mymall.search.config.MyMallElasitcSearchConfig;
import com.tracy.mymall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class ProductSaveServiceImpl implements ProductSaveService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    /**
     * 向es中保存信息
     * @param skuEsDtos
     * @return
     */
    @Override
    public boolean productStatusUp(List<SkuEsDto> skuEsDtos) throws IOException {
        // 批量保存
        BulkRequest bulkRequest = new BulkRequest();

        for (SkuEsDto skuEsDto : skuEsDtos) {
            IndexRequest request = new IndexRequest();
            request.index(ProductConst.PRODUCT_ES_INDEX).id(skuEsDto.getSkuId().toString());

            request.source(JSON.toJSONString(skuEsDto), XContentType.JSON);
            bulkRequest.add(request);
        }
//        发送批量保存消息
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, MyMallElasitcSearchConfig.COMMON_OPTIONS);
        log.info(bulk.toString());
        //返回是否成功
        return !bulk.hasFailures();
    }
}
