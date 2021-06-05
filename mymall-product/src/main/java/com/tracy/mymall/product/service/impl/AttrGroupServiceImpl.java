package com.tracy.mymall.product.service.impl;

import com.tracy.mymall.product.entity.AttrAttrgroupRelationEntity;
import com.tracy.mymall.product.entity.AttrEntity;
import com.tracy.mymall.product.service.AttrAttrgroupRelationService;
import com.tracy.mymall.product.service.AttrService;
import com.tracy.mymall.product.service.CategoryService;
import com.tracy.mymall.product.vo.AttrAttrgroupVo;
import com.tracy.mymall.product.vo.AttrGroupAttrRespVo;
import com.tracy.mymall.product.vo.SkuItemVo;
import com.tracy.mymall.product.vo.SpuItemGroupAttrVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;
    @Autowired
    private AttrService attrService;
    @Autowired
    private CategoryService categoryService;
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

    @Override
    public List<AttrEntity> getAttrGroupsAttr(Map<String, Object> params, Long attrgroupId) {
        List<AttrAttrgroupRelationEntity> attrGroupRelation = attrAttrgroupRelationService.list(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));
        List<Long> attrIds = attrGroupRelation.stream().map(relation -> relation.getAttrId()).collect(Collectors.toList());
        if (!attrIds.isEmpty()) {
            List<AttrEntity> attrEntities = (List<AttrEntity>) attrService.listByIds(attrIds);
            return attrEntities;
        }
        return new ArrayList<>();

    }

    /**
     * 查询同一分类下没有被分组关联的属性
     * @param params
     * @param attrgroupId
     * @return
     */
    @Override
    public PageUtils getAttrGroupsAttrNoRelation(Map<String, Object> params, Long attrgroupId) {
        // 查询分类
        AttrGroupEntity groupEntity = this.getById(attrgroupId);
        Long catelogId = groupEntity.getCatelogId();

        // 查询同一分类下的所有分组,包括当前分组
        List<AttrGroupEntity> groups = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));

        List<Long> groupIds = groups.stream().map(AttrGroupEntity::getAttrGroupId).collect(Collectors.toList());

        List<AttrAttrgroupRelationEntity> relations = attrAttrgroupRelationService.list(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", groupIds));

        List<Long> attrIds = relations.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
        // 分页查询分组没有关联的属性
        PageUtils pageUtils = attrService.queryNotRelatedPage(params, attrIds, catelogId);
        return pageUtils;
    }

    /**
     * 添加关联关系
     * @param attrAttrgroupVos
     */
    @Override
    public void addRelation(List<AttrAttrgroupVo> attrAttrgroupVos) {
        List<AttrAttrgroupRelationEntity> groups = attrAttrgroupVos.stream().map(item -> (AttrAttrgroupRelationEntity) item).collect(Collectors.toList());
        attrAttrgroupRelationService.saveBatch(groups);
    }

    /**
     * 根据分类id查询属性分组和每个分组下的属性
     * @param catelogId
     * @return
     */
    @Override
    public List<AttrGroupAttrRespVo> getAttrGroupAttrByCatelogId(Long catelogId) {
        // 1. 根据分类id查出所欲分组
        List<AttrGroupEntity> groups = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        if (!groups.isEmpty()) {
            // 2. 查询每个分组下的属性
            List<Long> groupIds = groups.stream().map(AttrGroupEntity::getAttrGroupId).collect(Collectors.toList());
            List<AttrAttrgroupRelationEntity> relations = attrAttrgroupRelationService.list(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", groupIds));
            List<Long> attrIds = relations.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
            List<AttrEntity> attrEntities = new ArrayList<>();
            if (!attrIds.isEmpty()) {
                attrEntities = (List<AttrEntity>) attrService.listByIds(attrIds);

            }
            // 遍历写入
            List<AttrEntity> finalAttrEntities = attrEntities;
            List<AttrGroupAttrRespVo> collect = groups.stream().map(group -> {
                AttrGroupAttrRespVo respVo = new AttrGroupAttrRespVo();
                respVo.setAttrs(new ArrayList<>());
                BeanUtils.copyProperties(group, respVo);
                relations.stream().forEach(item -> {
                    if (item.getAttrGroupId().equals(respVo.getAttrGroupId())) {
                        for (int i = 0; i < finalAttrEntities.size(); i++) {
                            if (item.getAttrId().equals(finalAttrEntities.get(i).getAttrId())) {
                                respVo.getAttrs().add(finalAttrEntities.get(i));
                            }
                        }
                    }
                });
                return respVo;
            }).collect(Collectors.toList());
            return collect;
        }
        return new ArrayList<>();
    }

    @Override
    public List<SpuItemGroupAttrVo> getAttrGroupAttrBySpuId(long spuId, long catelogId) {
        return this.baseMapper.getAttrGroupAttrBySpuId(spuId, catelogId);
    }

    @Override
    public void batchDeleteRelation(List<AttrAttrgroupVo> asList) {
        List<AttrAttrgroupRelationEntity> collect = asList.stream().map(entity -> (AttrAttrgroupRelationEntity) entity).collect(Collectors.toList());
        this.attrAttrgroupRelationService.deleteBatchRelations(collect);
    }

}