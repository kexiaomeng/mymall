package com.tracy.mymall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import com.tracy.mymall.common.valid.AddGroup;
import com.tracy.mymall.common.valid.MyListStrict;
import com.tracy.mymall.common.valid.UpdateGroup;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * 品牌
 * 
 * @author kexiaomeng
 * @email kexiaomeng@foxmail.com
 * @date 2021-04-22 00:34:22
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */
	@NotNull(message = "修改时品牌id不能为空",groups = {UpdateGroup.class})
	@Null(message = "添加时品牌id必须为空",groups = {AddGroup.class})
	@TableId
	private Long brandId;
	/**
	 * 品牌名
	 */
	@NotBlank(message = "品牌名不能为空", groups = {AddGroup.class})
	private String name;
	/**
	 * 品牌logo地址
	 * 如果在校验时@Validated指定了执行分组，如果某个校验规则没有分组，则不会匹配到
	 * @URL(message = "url不符合规范",groups = {AddGroup.class, UpdateGroup.class})表示修改时可以不携带，但是如果带了，必须是url地址
	 */
	@NotEmpty(message = "logo url不能为空",groups = {AddGroup.class})
	@URL(message = "url不符合规范",groups = {AddGroup.class, UpdateGroup.class})
	private String logo;
	/**
	 * 介绍
	 */
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	@MyListStrict(value = {0,1})
	private Integer showStatus;
	/**
	 * 检索首字母
	 */
	@NotNull(groups = {AddGroup.class})
	@Pattern(regexp = "^[a-zA-Z]$",groups = {UpdateGroup.class, AddGroup.class})
	private String firstLetter;
	/**
	 * 排序
	 */
	@NotNull(groups = {AddGroup.class})
	@Min(value = 0,groups = {UpdateGroup.class, AddGroup.class})
	private Integer sort;

}
