package com.tracy.mymall.ware.service.impl;

import com.tracy.mymall.common.constant.WareConst;
import com.tracy.mymall.ware.entity.PurchaseDetailEntity;
import com.tracy.mymall.ware.service.PurchaseDetailService;
import com.tracy.mymall.ware.service.WareSkuService;
import com.tracy.mymall.ware.vo.MergeVo;
import com.tracy.mymall.ware.vo.PurchaseFinishVo;
import com.tracy.mymall.ware.vo.PurchaseItemVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tracy.mymall.common.utils.PageUtils;
import com.tracy.mymall.common.utils.Query;

import com.tracy.mymall.ware.dao.PurchaseDao;
import com.tracy.mymall.ware.entity.PurchaseEntity;
import com.tracy.mymall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDetailService purchaseDetailService;

    @Autowired
    private WareSkuService wareSkuService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> queryWrapper = new QueryWrapper<>();
        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            queryWrapper.eq("status", status);
        }

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key) ) {
            queryWrapper.and( query -> {
                query.like("assignee_name", key).or().like("phone",key);
            });
        }


        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils unreceiveList(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 0).or().eq("status", 1);

        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void merge(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        if (purchaseId == null) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConst.PurchaseStatusEnum.CREATED.getCode());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }

        List<Long> items = mergeVo.getItems();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> collect = items.stream().map(item -> {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            detailEntity.setId(item.longValue());
            detailEntity.setPurchaseId(finalPurchaseId);
            detailEntity.setStatus(WareConst.PurchaseDetailStatusEnum.ASSIGNED.getCode());
            return detailEntity;
        }).collect(Collectors.toList());

        purchaseDetailService.updateBatchById(collect);
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        this.updateById(purchaseEntity);

    }

    /**
     * 领取采购单
     * @param purchaseItems
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receivePurchaseItem(List<Long> purchaseItems) {
        // 1. 检查各个采购单的状态，新建或者分配才可以领取
        List<PurchaseEntity> purchaseEntities = (List<PurchaseEntity>) this.listByIds(purchaseItems);

        List<PurchaseEntity> validItems = purchaseEntities.stream().filter(item -> item.getStatus() == WareConst.PurchaseStatusEnum.CREATED.getCode() || item.getStatus() == WareConst.PurchaseStatusEnum.ASSIGNED.getCode()).map(item -> {
            PurchaseEntity entity = new PurchaseEntity();
            BeanUtils.copyProperties(item, entity);
            entity.setStatus(WareConst.PurchaseStatusEnum.RECEIVE.getCode());
            return entity;
        }).collect(Collectors.toList());


        List<PurchaseDetailEntity> validPurchaseDetailIds = validItems.stream().flatMap(item -> {
            List<PurchaseDetailEntity> itemsByPurchaseId = purchaseDetailService.getItemsByPurchaseId(item.getId());
            for (PurchaseDetailEntity entity: itemsByPurchaseId) {
                entity.setStatus(WareConst.PurchaseDetailStatusEnum.BUYING.getCode());
            }
            return itemsByPurchaseId.stream();
        }).collect(Collectors.toList());

        // 2. 更新采购单的状态为已领取
        this.updateBatchById(validItems);


        // 3.更新采购单状态
        purchaseDetailService.updateBatchById(validPurchaseDetailIds);

    }

    /**
     * 采购完成，需要将采购的数据入库
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishPurchase(PurchaseFinishVo purchaseFinishVo) {

        AtomicBoolean finishSuccess = new AtomicBoolean(true);
        // 1. 更改采购项状态
        List<PurchaseItemVo> items = purchaseFinishVo.getItems();

        List<PurchaseDetailEntity> purchaseDetailEntities = items.stream().map(item -> {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            detailEntity.setId(item.getItemId());
            detailEntity.setStatus(item.getStatus());


            if (item.getStatus() != WareConst.PurchaseDetailStatusEnum.FINISH.getCode()) {
                finishSuccess.set(false);
            }else {
                // 增加库存
                PurchaseDetailEntity byId = purchaseDetailService.getById(item.getItemId());
                wareSkuService.addStock(byId.getSkuId(), byId.getWareId(), byId.getSkuNum());
            }
            return detailEntity;
        }).collect(Collectors.toList());
        purchaseDetailService.updateBatchById(purchaseDetailEntities);


        // 2. 更改采购单状态,只有所有采购项都正常结束才算正常
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseFinishVo.getId());
        if (finishSuccess.get()) {
            purchaseEntity.setStatus(WareConst.PurchaseStatusEnum.FINISH.getCode());
        }else {
            purchaseEntity.setStatus(WareConst.PurchaseStatusEnum.HASERROR.getCode());

        }
        this.updateById(purchaseEntity);

        // 3. 采购的数据入库
    }

}