package com.tracy.mymall.common.constant;

public class ProductConst {
    public static String PRODUCT_ES_INDEX = "product";
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

    public enum PublishStatusEnum{
        NEW_PRO(0, "新建"),
        PRODUCT_UP(1, "上架"),
        PRODUCT_DOWN(2, "下加");
        private int type;
        private String desc;
        private PublishStatusEnum(int attrType, String desc){
            this.type = attrType;
            this.desc = desc;
        }
        public int getType(){
            return type;
        }

    }
}
