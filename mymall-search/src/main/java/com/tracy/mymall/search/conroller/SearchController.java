package com.tracy.mymall.search.conroller;

import com.tracy.mymall.search.service.MymallSearchService;
import com.tracy.mymall.search.vo.SearchParam;
import com.tracy.mymall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class SearchController {
    @Autowired
    private MymallSearchService mymallSearchService;

    @GetMapping("/list.html")
    public String listPage(HttpServletRequest request, SearchParam searchParam, Model model) {
        searchParam.setQueryString(request.getQueryString());
        SearchResult result = mymallSearchService.search(searchParam);
        model.addAttribute("result", result);
        return "list";
    }
}
