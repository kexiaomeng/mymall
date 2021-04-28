package com.tracy.mymall.product.common;

import javafx.scene.chart.CategoryAxis;
import lombok.Getter;

public enum CategoryLevelEnum {
    DEFAULT(0,"顶级Parent"),
    LEVEL_ONE(1,"一级分类"),
    LEVEL_TWO(1,"二级分类"),
    LEVEL_THREE(1,"三级分类");

    private @Getter long number;
    private @Getter String desc;

    private CategoryLevelEnum(long number, String desc) {
        this.number = number;
        this.desc = desc;
    }
    public static CategoryLevelEnum getCategoryLevelByLevelId(long levelId) {
        for (CategoryLevelEnum value : CategoryLevelEnum.values()) {
            if (value.getNumber() == levelId) {
                return value;
            }
        }
        return DEFAULT;
    }
}
