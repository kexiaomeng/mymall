package com.tracy.mymall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.tracy.mymall.common.constant.ProductConst;
import com.tracy.mymall.common.dto.es.SkuEsDto;
import com.tracy.mymall.common.utils.R;
import com.tracy.mymall.search.config.MyMallElasitcSearchConfig;
import com.tracy.mymall.search.feign.ProductFeignService;
import com.tracy.mymall.search.service.MymallSearchService;
import com.tracy.mymall.search.vo.SearchParam;
import com.tracy.mymall.search.vo.SearchResult;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.Nested;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MymallSearchServiceImpl implements MymallSearchService {
    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private ProductFeignService feignService;

    /**
     * ???es??????????????????
     * @param searchParam
     * @return
     */
    @Override
    public SearchResult search(SearchParam searchParam) {
        // ????????????????????????DSL??????
        SearchResult result = null;
        // 1. ??????????????????
        SearchRequest request = buildSearchRequest(searchParam);

        try {
            // 2.??????????????????
            SearchResponse response = client.search(request, MyMallElasitcSearchConfig.COMMON_OPTIONS);
            // 3.???????????????????????????????????????
            result = buildSearchResult(response, searchParam);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private SearchResult buildSearchResult(SearchResponse response, SearchParam param) {
        Map<Long, String> attrMap = new HashMap<>();
        SearchResult result = new SearchResult();
        List<SkuEsDto> skuEsDtos = new ArrayList<>();
        for (SearchHit hit : response.getHits().getHits()) {

            String sourceAsString = hit.getSourceAsString();
            SkuEsDto skuEsDto = JSON.parseObject(sourceAsString, SkuEsDto.class);
            // TODO ?????????????????????
            if (StringUtils.isNotEmpty(param.getKeyword())) {
                HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                Text fragment = skuTitle.getFragments()[0];
                skuEsDto.setSkuTitle(fragment.string());

            }
            skuEsDtos.add(skuEsDto);
        }
        // ?????????????????????
        result.setProduct(skuEsDtos);

        // ??????????????????????????????
        Aggregations aggregations = response.getAggregations();
        // 1. ??????????????????
        Terms catalogAgg = aggregations.get("catalog_agg");
        List<SearchResult.CatalogVo> catalogVos = catalogAgg.getBuckets().stream().map(item -> {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            long catalogId = item.getKeyAsNumber().longValue();
            catalogVo.setCatalogId(catalogId);
            // ?????????????????????
            Terms catalogNameAgg = item.getAggregations().get("catalog_name_agg");
            Terms.Bucket bucket = catalogNameAgg.getBuckets().get(0);
            String catalogName = bucket.getKeyAsString();
            catalogVo.setCatalogName(catalogName);
            return catalogVo;

        }).collect(Collectors.toList());
        result.setCatalogs(catalogVos);

        // 2. ??????????????????
        Terms brandAgg = aggregations.get("brand_agg");
        List<SearchResult.BrandVo> brandVos = brandAgg.getBuckets().stream().map(item -> {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            long brandId = item.getKeyAsNumber().longValue();
            brandVo.setBrandId(brandId);
            // ?????????????????????
            Terms brandNameAgg = item.getAggregations().get("brand_name_agg");
            Terms.Bucket bucket = brandNameAgg.getBuckets().get(0);
            String brandName = bucket.getKeyAsString();
            brandVo.setBrandName(brandName);
            // ?????????????????????
            Terms brandImgAgg = item.getAggregations().get("brand_img_agg");
            Terms.Bucket bucketImg = brandImgAgg.getBuckets().get(0);
            String brandImg = bucketImg.getKeyAsString();
            brandVo.setBrandImg(brandImg);

            return brandVo;

        }).collect(Collectors.toList());
        result.setBrands(brandVos);

        // ?????????????????? nested
        Nested nested = aggregations.get("attr_agg");
        // nested??????agg
        Terms attrIdAgg = nested.getAggregations().get("attr_id_agg");
        List<SearchResult.AttrVo> attrVos = attrIdAgg.getBuckets().stream().map(item -> {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            // ????????????ID
            long attrId = item.getKeyAsNumber().longValue();
            // attrid??????????????????????????????
            Terms attrNameAgg = item.getAggregations().get("attr_name_agg");
            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
            attrMap.put(attrId, attrName);
            Terms attrValueAgg = item.getAggregations().get("attr_value_agg");
            List<String> attrValues = attrValueAgg.getBuckets().stream().map(valueItem -> valueItem.getKeyAsString()).collect(Collectors.toList());

            attrVo.setAttrId(attrId);
            attrVo.setAttrName(attrName);
            attrVo.setAttrValue(attrValues);
            return attrVo;
        }).collect(Collectors.toList());
        result.setAttrs(attrVos);

        long total = response.getHits().getTotalHits().value;
        result.setPageNum(param.getPageNum());
        result.setTotal(total);
        int totalPages = (int) ((total - 1) / ProductConst.PRODUCT_ES_PAGE + 1);
        result.setTotalPages(totalPages);
        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);

        // ?????????????????????????????????
        List<String> attrs = param.getAttrs();
        if (attrs != null && !attrs.isEmpty()) {
            List<SearchResult.NavVo> collect = attrs.stream().map(item -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] s = item.split("_");
                // ??????????????????id???????????????
                String attrId = s[0];

                navVo.setNavValue(s[1]);
                R info = feignService.info(Long.parseLong(attrId));
                System.out.println(info.get("attr"));
                navVo.setNavName(attrMap.get(Long.parseLong(attrId)));
                String encode = null;
                try {
                    encode = URLEncoder.encode(item, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                String queryString = param.getQueryString();

                String replace = queryString.replace("&attrs=" + encode, "").replace("attrs=" + encode + "&", "").replace("attrs=" + encode, "");
                navVo.setLink("http://search.mymall.com:1111/list.html" + (replace.isEmpty() ? "" : "?" + replace));
                return navVo;
            }).collect(Collectors.toList());

            result.setNavs(collect);
        }
        System.out.println("result:"+result);

        return result;
    }

    /**
     * # ???????????????skuTitle???  ???????????????????????????????????????????????????????????? ???????????????????????????????????????,filter???????????????????????????,????????????????????????????????????????????????????????????????????????????????????
     * @param searchParam
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam searchParam) {
        SearchRequest request = new SearchRequest();
        request.indices(ProductConst.PRODUCT_ES_INDEX);
        // ??????SearchBuilder??????DSL?????????dsl.json
        SearchSourceBuilder builder = new SearchSourceBuilder();
        request.source(builder);

        // boolbuilder????????????
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        builder.query(boolQueryBuilder);

        // 1. ??????????????????
        if (StringUtils.isNotEmpty(searchParam.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", searchParam.getKeyword()));
        }
        // 2. ??????filter??????
        // 2.1 ??????catalogId??????
        if (searchParam.getCatalog3Id() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId", searchParam.getCatalog3Id()));
        }
        // 2.2 ??????brandId?????????brand???????????????
        if (searchParam.getBrandId() != null && !searchParam.getBrandId().isEmpty()) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", searchParam.getBrandId()));

        }
        // 2.3??????????????????
        boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock", false));

//        boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock", searchParam.getHasStock() == null || searchParam.getHasStock() == 1 ? true : false));
        // 2.4 ??????????????????
        if (StringUtils.isNotEmpty(searchParam.getSkuPrice())) {
            RangeQueryBuilder priceRangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String skuPrice = searchParam.getSkuPrice();
            if (skuPrice.startsWith("_")) {
                //??????????????????????????????????????????""?????????
               priceRangeQuery.lte(skuPrice.split("_")[1]);
            }else if (skuPrice.endsWith("_")) {
                priceRangeQuery.gte(skuPrice.split("_")[0]);

            }else {
                priceRangeQuery.gte(skuPrice.split("_")[0]).lte(skuPrice.split("_")[1]);

            }

            boolQueryBuilder.filter(priceRangeQuery);
        }


        // 2.5 ?????????????????????
        if (searchParam.getAttrs() != null && !searchParam.getAttrs().isEmpty()) {
            // ?????????attr?????????????????????????????????
            // attrs=1_5???:8???&attrs=2_16G:8G
            for (String attr : searchParam.getAttrs()) {
                String[] attrArr = attr.split("_");
                String[] attrValueArr = attrArr[1].split(":");
                BoolQueryBuilder nestBoolQuery = QueryBuilders.boolQuery();
                nestBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrArr[0]));
                nestBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValueArr));
//                ?????????attr?????????????????????nested??????
                NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery("attrs", nestBoolQuery, ScoreMode.None);
                boolQueryBuilder.filter(nestedQueryBuilder);
            }
        }

        // 2.6???????????????query??????

        builder.query(boolQueryBuilder);

        // 3. ????????????????????????
        // 3.1 ??????
        if (StringUtils.isNotEmpty(searchParam.getSort())) {
            // ??????????????? hotScore:asc/desc
            String sort = searchParam.getSort();
            String[] sortArr = sort.split("_");
            builder.sort(sortArr[0], SortOrder.fromString(sortArr[1]));
        }

        // 3.2 ??????
        int pageNum = searchParam.getPageNum() == null ? 1 : searchParam.getPageNum();

        builder.from((pageNum - 1) * ProductConst.PRODUCT_ES_PAGE);
        builder.size(ProductConst.PRODUCT_ES_PAGE);

        // 3.3 ??????
        if (StringUtils.isNotEmpty(searchParam.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            builder.highlighter(highlightBuilder);
        }


        // 4. ??????
        // 4.1 ????????????id??????---????????????brand_name,brand_img
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg");
        brandAgg.field("brandId").size(50);
        brandAgg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brandAgg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));

        builder.aggregation(brandAgg);
        // 4.2 ????????????id??????---????????????catalog_name
        TermsAggregationBuilder catalogIdAgg = AggregationBuilders.terms("catalog_agg");
        catalogIdAgg.field("catalogId").size(50);
        catalogIdAgg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));

        builder.aggregation(catalogIdAgg);

        // 4.3 ?????????????????????nested
        NestedAggregationBuilder attrNestedAgg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attr_id_agg");
        attrIdAgg.field("attrs.attrId").size(50);
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(10));
        // ?????????????????????????????????value
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));

        // netsted?????????
        attrNestedAgg.subAggregation(attrIdAgg);

        builder.aggregation(attrNestedAgg);
        System.out.println("?????????DSL:"+builder.toString());

        return  request;
    }
}
