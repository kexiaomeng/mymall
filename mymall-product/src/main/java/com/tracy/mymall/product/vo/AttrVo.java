package com.tracy.mymall.product.vo;

import com.tracy.mymall.product.entity.AttrEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 属性vo对象
 */
@Getter
@Setter
public class AttrVo extends AttrEntity {
    /**
     * 属性分组id
     */
    private Long attrGroupId;
}
