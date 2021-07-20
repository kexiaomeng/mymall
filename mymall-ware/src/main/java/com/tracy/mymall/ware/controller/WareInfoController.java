package com.tracy.mymall.ware.controller;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.tracy.mymall.ware.feign.MyMallMemberFeignService;
import com.tracy.mymall.ware.vo.FareVo;
import com.tracy.mymall.ware.vo.MemberReceiveAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tracy.mymall.ware.entity.WareInfoEntity;
import com.tracy.mymall.ware.service.WareInfoService;
import com.tracy.mymall.common.utils.PageUtils;
import com.tracy.mymall.common.utils.R;



/**
 * 仓库信息
 *
 * @author kexiaomeng
 * @email kexiaomeng@foxmail.com
 * @date 2021-04-22 01:29:49
 */
@RestController
@RequestMapping("ware/wareinfo")
public class WareInfoController {
    @Autowired
    private MyMallMemberFeignService myMallMemberFeignService;
    @Autowired
    private WareInfoService wareInfoService;


    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareInfoService.queryPageByCondition(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		WareInfoEntity wareInfo = wareInfoService.getById(id);

        return R.ok().put("wareInfo", wareInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody WareInfoEntity wareInfo){
		wareInfoService.save(wareInfo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody WareInfoEntity wareInfo){
		wareInfoService.updateById(wareInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		wareInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    /**
     * 查运费
     */
    @RequestMapping("/fare/{addressId}")
    public R fare(@PathVariable("addressId") Long addressId){
        R info = myMallMemberFeignService.info(addressId);
        FareVo fareVo = new FareVo();
        if (info.getCode() == 0) {
            Object memberReceiveAddress = info.get("memberReceiveAddress");
            String string = JSON.toJSONString(memberReceiveAddress);
            MemberReceiveAddress address = JSON.parseObject(string, new TypeReference<MemberReceiveAddress>(){});
            fareVo.setAddress(address);
            fareVo.setPayPrice(new BigDecimal("20"));
            // TODO 处理运费信息
        }else {
            fareVo.setPayPrice(new BigDecimal("20"));

        }
        return R.ok().put("data",fareVo);

    }

}
