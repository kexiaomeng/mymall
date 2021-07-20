package com.tracy.mymall.cart.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车中的购物项，按照sku区分,totalprice需要计算得到
 */
public class CartItem {
    private Long skuId;
    /**
     * 是否被选中
     */
    private Boolean check;
    /**
     * sku属性
     */
    private List<String> skuAttr;
    private String title;
    private String image;

    private Integer count;

    private BigDecimal price;
    /**
     * totalPrice由count和price共同计算出来
     */
    private BigDecimal totalPrice;

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public Boolean getCheck() {
        return check;
    }

    public void setCheck(Boolean checked) {
        this.check = checked;
    }

    public List<String> getSkuAttr() {
        return skuAttr;
    }

    public void setSkuAttr(List<String> skuAttr) {
        this.skuAttr = skuAttr;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getTotalPrice() {
        BigDecimal total = this.getPrice().multiply(new BigDecimal(""+this.getCount()));
        return total;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}
