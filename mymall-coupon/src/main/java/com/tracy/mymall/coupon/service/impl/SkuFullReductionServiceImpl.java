package com.tracy.mymall.coupon.service.impl;

import com.tracy.mymall.common.dto.MemberPrice;
import com.tracy.mymall.common.dto.SkuReductionDto;
import com.tracy.mymall.coupon.entity.MemberPriceEntity;
import com.tracy.mymall.coupon.entity.SkuLadderEntity;
import com.tracy.mymall.coupon.service.MemberPriceService;
import com.tracy.mymall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tracy.mymall.common.utils.PageUtils;
import com.tracy.mymall.common.utils.Query;

import com.tracy.mymall.coupon.dao.SkuFullReductionDao;
import com.tracy.mymall.coupon.entity.SkuFullReductionEntity;
import com.tracy.mymall.coupon.service.SkuFullReductionService;
import org.springframework.transaction.annotation.Transactional;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    private SkuLadderService skuLadderService;

    @Autowired
    private MemberPriceService memberPriceService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void saveRecudtion(SkuReductionDto skuReductionDto) {
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        BeanUtils.copyProperties(skuReductionDto,skuLadderEntity);
        skuLadderEntity.setAddOther(skuReductionDto.getCountStatus());
        skuLadderService.save(skuLadderEntity);

        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuReductionDto,skuFullReductionEntity);
        this.baseMapper.insert(skuFullReductionEntity);

        List<MemberPrice> memberPrice = skuReductionDto.getMemberPrice();
        List<MemberPriceEntity> collect = memberPrice.stream().map(item -> {
            MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
            memberPriceEntity.setSkuId(skuReductionDto.getSkuId());
            memberPriceEntity.setMemberLevelId(item.getId());
            memberPriceEntity.setMemberLevelName(item.getName());
            memberPriceEntity.setMemberPrice(item.getPrice());
            memberPriceEntity.setAddOther(skuReductionDto.getCountStatus());
            return memberPriceEntity;
        }).collect(Collectors.toList());

        memberPriceService.saveBatch(collect);
    }



}