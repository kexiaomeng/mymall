package com.tracy.mymall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.tracy.mymall.member.feign.RemoteCouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tracy.mymall.member.entity.MemberEntity;
import com.tracy.mymall.member.service.MemberService;
import com.tracy.mymall.common.utils.PageUtils;
import com.tracy.mymall.common.utils.R;



/**
 * 会员
 *
 * @author kexiaomeng
 * @email kexiaomeng@foxmail.com
 * @date 2021-04-22 01:23:53
 */
@RestController
@RequestMapping("member/member")
@RefreshScope
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    private RemoteCouponService service;

    @Value("${config.info}")
    private String name;

    @RequestMapping("/coupon/list")
    public R testGetCoupon(@RequestParam Map<String, Object> params) {
        R coupon = service.memberCouponList(params);
        return R.ok().put("member","sunmeng").put("coupon", coupon.get("coupon")).put("name", name);
    }
    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
