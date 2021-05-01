package com.tracy.mymall.product.service.impl;

import com.tracy.mymall.product.entity.AttrAttrgroupRelationEntity;
import com.tracy.mymall.product.entity.AttrGroupEntity;
import com.tracy.mymall.product.entity.CategoryEntity;
import com.tracy.mymall.product.service.AttrAttrgroupRelationService;
import com.tracy.mymall.product.service.AttrGroupService;
import com.tracy.mymall.product.service.CategoryService;
import com.tracy.mymall.product.vo.AttrRespVo;
import com.tracy.mymall.product.vo.AttrVo;
import org.apache.commons.lang3.StringUtils;
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

import com.tracy.mymall.product.dao.AttrDao;
import com.tracy.mymall.product.entity.AttrEntity;
import com.tracy.mymall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;
    @Autowired
    private AttrGroupService attrGroupService;
    @Autowired
    private CategoryService categoryService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 保存属性、属性和分组关联关系
     * @param attr
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveCascade(AttrVo attr) {
        AttrEntity entity = new AttrEntity();
        BeanUtils.copyProperties(attr, entity);
        this.save(entity);

        AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
        attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
        attrAttrgroupRelationEntity.setAttrId(entity.getAttrId());
        attrAttrgroupRelationService.save(attrAttrgroupRelationEntity);
    }

    /**
     * 需要查询出分类名和分组名
     */
    @Override
    public PageUtils queryBasePageByCategory(Map<String, Object> params, Long catelogId) {
        try {
            QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<>();

            IPage<AttrEntity> page = new Query<AttrEntity>().getPage(params);
            if (params.containsKey("key")) {
                String key = (String) params.get("key");
                if (StringUtils.isNotBlank(key)) {
                    queryWrapper.and((obj) -> obj.eq("attr_id",key).or().like("attr_name", key));
                }
            }
            IPage<AttrEntity> result;
            // 0表示查询所有
            if (catelogId == 0) {
                result = this.page(page, queryWrapper);
            }else {
                queryWrapper.eq("catelog_id", catelogId);
                result = this.page(page, queryWrapper);
            }
            // 需要查询出分类名和分组名
            List<AttrRespVo> attrRespVos = result.getRecords().stream().map(attrEntity -> {
                // 返回到前台的vo
                AttrRespVo attrRespVo = new AttrRespVo();
                BeanUtils.copyProperties(attrEntity, attrRespVo);

                // 查询分组名称，先去关联表查询id，再去分组表查询名称
                AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationService.queryByAttrId(attrEntity.getAttrId());
                if (attrAttrgroupRelationEntity != null) {
                    AttrGroupEntity attrGroup = attrGroupService.getById(attrAttrgroupRelationEntity.getAttrGroupId());
                    attrRespVo.setGroupName(attrGroup.getAttrGroupName());
                }

                // 查询分类名称
                CategoryEntity categoryEntity = categoryService.getById(attrEntity.getCatelogId());

                attrRespVo.setCatelogName(categoryEntity.getName());
                return attrRespVo;
            }).collect(Collectors.toList());

            PageUtils pageUtils = new PageUtils(result);
            pageUtils.setList(attrRespVos);
            return pageUtils;
        }catch (Exception e) {
            log.error("",e);
        }
        return null;

    }

    @Override
    public AttrRespVo getAttrInfo(Long attrId) {
        AttrEntity attrEntity = this.baseMapper.selectById(attrId);
        AttrRespVo respVo = new AttrRespVo();
        BeanUtils.copyProperties(attrEntity,respVo);
        //查询并设置分组名
        AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationService.queryByAttrId(attrId);
        //如果分组id不为空。则查出分组名
        if (attrAttrgroupRelationEntity != null && attrAttrgroupRelationEntity.getAttrGroupId() != null) {
            AttrGroupEntity attrGroupEntity = attrGroupService.getOne(new QueryWrapper<AttrGroupEntity>().eq("attr_group_id", attrAttrgroupRelationEntity.getAttrGroupId()));
            //设置分组名
            respVo.setGroupName(attrGroupEntity.getAttrGroupName());
            respVo.setAttrGroupId(attrGroupEntity.getAttrGroupId());
        }
        //查询到分类信息
        CategoryEntity categoryEntity = categoryService.getById(attrEntity.getCatelogId());
        //设置分类名
        respVo.setCatelogName(categoryEntity.getName());
        //查询并设置分类路径
        Long[] catelogPathById = categoryService.findCategoryPath(categoryEntity.getCatId());
        respVo.setCategoryPath(catelogPathById);
        return respVo;
    }

    @Override
    public void updateAttrVo(AttrVo attr) {
        AttrEntity entity = new AttrEntity();
        BeanUtils.copyProperties(attr,entity);
        this.baseMapper.updateById(entity);
        //只有当属性分组不为空时，说明更新的是规则参数，则需要更新关联表
        if (attr.getAttrGroupId() != null) {
            //查询属性-分组名对应关系
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrId(attr.getAttrId());
            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
            attrAttrgroupRelationService.saveOrUpdate(attrAttrgroupRelationEntity);
        }
    }

}