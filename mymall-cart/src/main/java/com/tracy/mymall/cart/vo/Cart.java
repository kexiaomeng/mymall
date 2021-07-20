package com.tracy.mymall.cart.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车，包含了购物项，部分属性需要通过计算得到
 */
public class Cart {
    private List<CartItem> items;
    /**
     * 商品的总数量
      */
    private Integer countNum;
    /**
     * 商品类型的数量
      */
    private Integer countType;
    /**
     * 商品的总价
     */
    private BigDecimal totalAmount;
    /**
     * 商品减免的价格
     */
    private BigDecimal reduce = new BigDecimal(0);

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        int count = 0;
        if (this.getItems() != null) {
            for (CartItem item : this.getItems()) {
                count += item.getCount();
            }
        }
        return count;
    }


    public Integer getCountType() {
        int count = 0;

        if (this.getItems() != null) {
            count = this.getItems().size();
        }
        return count;
    }


    public BigDecimal getTotalAmount() {
        BigDecimal amount = new BigDecimal("0");
        if(this.items != null && this.items.size() > 0){
            for (CartItem item : this.items) {
                // 只有选中的才可以计算
                if(item.getCheck()){
                    BigDecimal totalPrice = item.getTotalPrice();
                    amount = amount.add(totalPrice);
                }
            }
        }
        return amount.subtract(this.getReduce());

    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
