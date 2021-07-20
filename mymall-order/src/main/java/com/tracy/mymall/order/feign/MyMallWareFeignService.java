package com.tracy.mymall.order.feign;

import com.tracy.mymall.common.utils.R;
import com.tracy.mymall.common.vo.WareLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient("mymall-ware")
public interface MyMallWareFeignService {
    @PostMapping("/ware/waresku/hasStock")
    R hasStock(@RequestBody List<Long> skuIds);

    /**
     * 查运费
     */
    @RequestMapping("ware/wareinfo/fare/{addressId}")
    R fare(@PathVariable("addressId") Long addressId);

    @PostMapping("/ware/waresku/lockStock")
    R lockStock(@RequestBody WareLockVo wareLockVo) ;
}
