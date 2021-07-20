package com.tracy.mymall.member.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebMemberController {

    @GetMapping("memberOrder.html")
    public String memberOrder() {
        return "orderList";
    }
}
