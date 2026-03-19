package com.musclegrow.constant;

/**
 * 优惠券状态常量
 */
public class VoucherStatusConstant {

    private VoucherStatusConstant() {
    }

    /**
     * 投放中
     */
    public static final Integer ON_SALE = 1;

    /**
     * 已结束
     */
    public static final Integer ENDED = 2;

    /**
     * 已下架
     */
    public static final Integer OFF_SHELF = 3;

    /**
     * 待开始，仅作为展示/筛选状态使用，不落库
     */
    public static final Integer NOT_STARTED = 4;
}
