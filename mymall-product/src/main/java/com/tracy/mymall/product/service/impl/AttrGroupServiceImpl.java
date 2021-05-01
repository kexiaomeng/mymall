package com.tracy.mymall.product.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tracy.mymall.common.utils.PageUtils;
import com.tracy.mymall.common.utils.Query;

import com.tracy.mymall.product.dao.AttrGroupDao;
import com.tracy.mymall.product.entity.AttrGroupEntity;
import com.tracy.mymall.product.service.AttrGroupService;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, long catelogId) {

        QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<>();

        IPage<AttrGroupEntity> page = new Query<AttrGroupEntity>().getPage(params);
        if (params.containsKey("key")) {
            String key = (String) params.get("key");
            if (StringUtils.isNotBlank(key)) {
                // select * from attr_group where catelogId=? and (attrGroupId=? or attrGroupName like "%%")
                queryWrapper.and((obj) -> {
                    obj.eq("attr_group_id",key).or().like("attr_group_name", key);
                });
            }
        }

        // 0表示查询所有
        if (catelogId == 0) {
            IPage<AttrGroupEntity> result = this.page(page, queryWrapper);
            return new PageUtils(result);
        }else {
            queryWrapper.eq("catelog_id", catelogId);

            IPage<AttrGroupEntity> result = this.page(page, queryWrapper);
            return new PageUtils(result);

        }
    }

}