package com.tracy.mymall.ware.service.impl;

import com.tracy.mymall.ware.entity.WareSkuEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tracy.mymall.common.utils.PageUtils;
import com.tracy.mymall.common.utils.Query;

import com.tracy.mymall.ware.dao.WareOrderTaskDetailDao;
import com.tracy.mymall.ware.entity.WareOrderTaskDetailEntity;
import com.tracy.mymall.ware.service.WareOrderTaskDetailService;


@Service("wareOrderTaskDetailService")
public class WareOrderTaskDetailServiceImpl extends ServiceImpl<WareOrderTaskDetailDao, WareOrderTaskDetailEntity> implements WareOrderTaskDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {



        IPage<WareOrderTaskDetailEntity> page = this.page(
                new Query<WareOrderTaskDetailEntity>().getPage(params),
                new QueryWrapper<WareOrderTaskDetailEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void updateWareTaskDetailState(Long detailId, int state) {
        WareOrderTaskDetailEntity detailEntity = new WareOrderTaskDetailEntity();
        detailEntity.setId(detailId);
        detailEntity.setLockStatus(2);
        this.baseMapper.updateById(detailEntity);
    }

    @Override
    public List<WareOrderTaskDetailEntity> queryDetailListByTaskId(Long taskId) {
        return this.list(new QueryWrapper<WareOrderTaskDetailEntity>().eq("task_id", taskId));
    }

}