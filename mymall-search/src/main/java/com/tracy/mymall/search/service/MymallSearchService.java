package com.tracy.mymall.search.service;

import com.tracy.mymall.search.vo.SearchParam;
import com.tracy.mymall.search.vo.SearchResult;

public interface MymallSearchService {
    /**
     * 根据一堆条件查询
     * @param searchParam
     * @return
     */
    SearchResult search(SearchParam searchParam);
}
