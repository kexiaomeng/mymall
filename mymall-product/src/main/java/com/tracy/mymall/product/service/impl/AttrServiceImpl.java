package com.tracy.mymall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.tracy.mymall.common.constant.ProductConst;
import com.tracy.mymall.product.entity.*;
import com.tracy.mymall.product.service.*;
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
import org.springframework.transaction.annotation.Transactional;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;
    @Autowired
    private AttrGroupService attrGroupService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ProductAttrValueService productAttrValueService;
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
        if (attr.getAttrType() == ProductConst.AttrEnum.BASE_TYPE.getAttrType() && attr.getAttrGroupId() != null) {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
            attrAttrgroupRelationEntity.setAttrId(entity.getAttrId());
            attrAttrgroupRelationService.save(attrAttrgroupRelationEntity);
        }

    }

    /**
     * 需要查询出分类名和分组名
     */
    @Override
    public PageUtils queryBasePageByCategory(Map<String, Object> params, Long catelogId, String attrType) {
        try {
            QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("attr_type", "base".equalsIgnoreCase(attrType) ? ProductConst.AttrEnum.BASE_TYPE.getAttrType() :ProductConst.AttrEnum.SALE_TYPE.getAttrType());

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
                if (attrEntity.getAttrType() == ProductConst.AttrEnum.BASE_TYPE.getAttrType()) {
                    // 查询分组名称，先去关联表查询id，再去分组表查询名称
                    AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationService.queryByAttrId(attrEntity.getAttrId());
                    if (attrAttrgroupRelationEntity != null) {
                        AttrGroupEntity attrGroup = attrGroupService.getById(attrAttrgroupRelationEntity.getAttrGroupId());
                        attrRespVo.setGroupName(attrGroup.getAttrGroupName());
                    }
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
        if (attrEntity.getAttrType() == ProductConst.AttrEnum.BASE_TYPE.getAttrType()) {
            //查询并设置分组名
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationService.queryByAttrId(attrId);
            //如果分组id不为空。则查出分组名
            if (attrAttrgroupRelationEntity != null && attrAttrgroupRelationEntity.getAttrGroupId() != null) {
                AttrGroupEntity attrGroupEntity = attrGroupService.getOne(new QueryWrapper<AttrGroupEntity>().eq("attr_group_id", attrAttrgroupRelationEntity.getAttrGroupId()));
                //设置分组名
                respVo.setGroupName(attrGroupEntity.getAttrGroupName());
                respVo.setAttrGroupId(attrGroupEntity.getAttrGroupId());
            }
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
        if (attr.getAttrType() == ProductConst.AttrEnum.BASE_TYPE.getAttrType()) {
            //查询属性-分组名对应关系
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrId(attr.getAttrId());
            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
            attrAttrgroupRelationService.saveOrUpdate(attrAttrgroupRelationEntity, new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", entity.getAttrId()));
        }
    }

    @Override
    public PageUtils queryNotRelatedPage(Map<String, Object> params, List<Long> attrIds, Long catelogId) {
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId).ne("attr_type", ProductConst.AttrEnum.SALE_TYPE.getAttrType());
        // 当前分类下是否有属性关联到分组
        if (!attrIds.isEmpty()) {
           queryWrapper.notIn("attr_id", attrIds);
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), queryWrapper);
        return new PageUtils(page);
    }

    @Override
    public List<ProductAttrValueEntity> listAttrsforSpu(Long spuId) {
        List<ProductAttrValueEntity> list = productAttrValueService.list(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));
        return list;
    }

    @Override
    public void updateSpuAttrs(Long spuId, List<ProductAttrValueEntity> attrValueEntities) {
        for (ProductAttrValueEntity attrValueEntity : attrValueEntities) {
            attrValueEntity.setSpuId(spuId);
            productAttrValueService.update(attrValueEntity,
                    new UpdateWrapper<ProductAttrValueEntity>().eq("spu_id", spuId).eq("attr_id", attrValueEntity.getAttrId()));
        }
    }

    @Override
    public List<Long> selectSearchIds(List<Long> attrIds) {
        return this.baseMapper.selectSearchIds(attrIds);
    }

}