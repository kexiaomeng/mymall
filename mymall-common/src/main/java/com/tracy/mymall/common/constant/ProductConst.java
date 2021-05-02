package com.tracy.mymall.common.constant;

public class ProductConst {
    public enum AttrEnum{
        BASE_TYPE(1, "规格属性"),
        SALE_TYPE(0, "销售属性");
        private int attrType;
        private String desc;
        private AttrEnum(int attrType, String desc){
            this.attrType = attrType;
            this.desc = desc;
        }
        public int getAttrType(){
            return attrType;
        }

    }
}
