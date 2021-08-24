package com.tracy.mymall.coupon.service.impl;

import com.tracy.mymall.coupon.entity.SeckillSkuRelationEntity;
import com.tracy.mymall.coupon.service.SeckillSkuRelationService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tracy.mymall.common.utils.PageUtils;
import com.tracy.mymall.common.utils.Query;

import com.tracy.mymall.coupon.dao.SeckillSessionDao;
import com.tracy.mymall.coupon.entity.SeckillSessionEntity;
import com.tracy.mymall.coupon.service.SeckillSessionService;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    private SeckillSkuRelationService seckillSkuRelationService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper conditionQueryWrapper = new QueryWrapper<SeckillSessionEntity>();
        String string = (String) params.get("promotionSessionId");
        if (StringUtils.isNotEmpty(string)) {
            conditionQueryWrapper.eq("promotion_session_id", string);
        }
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                conditionQueryWrapper
        );

        return new PageUtils(page);
    }

    /**
     * 如果活动不为空，就进行遍历，查询出活动下所有的商品
     * @return
     */
    @Override
    public List<SeckillSessionEntity> getLatest3DaysSession() {
        List<SeckillSessionEntity> seckillSessionEntities = this.list(new QueryWrapper<SeckillSessionEntity>().between("start_time", getStartTime(), getEndTime()));
        if (seckillSessionEntities != null) {
            seckillSessionEntities.forEach(session -> {
                List<SeckillSkuRelationEntity> promotion_session_id = seckillSkuRelationService.list(new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_session_id", session.getId()));
                session.setSkuRelationEntities(promotion_session_id);
            });
        }

        return seckillSessionEntities;
    }


    public String getStartTime(){
        LocalDateTime startTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        return startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String getEndTime() {
        LocalDateTime startTime = LocalDateTime.of(LocalDate.now().plusDays(2), LocalTime.MAX);
        return startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }


}