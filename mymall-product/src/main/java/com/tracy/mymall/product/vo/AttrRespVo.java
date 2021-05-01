package com.tracy.mymall.product.vo;

import com.tracy.mymall.product.entity.AttrEntity;
import lombok.Data;

/**
 * 页面查询返回的vo
 */
@Data
public class AttrRespVo extends AttrEntity {
    /**
     * 所属分类名字
     */
    private String catelogName;
    /**
     * 所属分组名字
     */
    private String groupName;

    /**
     * 分类路径
     */
    private Long[] categoryPath;
    private Long attrGroupId;

}
