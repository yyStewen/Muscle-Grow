package com.musclegrow.constant;

/**
 * 用户券仓状态常量
 */
public class VoucherStorageStatusConstant {

    private VoucherStorageStatusConstant() {
    }

    /**
     * 未使用
     */
    public static final Integer UNUSED = 1;

    /**
     * 已使用
     */
    public static final Integer USED = 2;

    /**
     * 已过期
     */
    public static final Integer EXPIRED = 3;

    /**
     * 锁定中
     */
    public static final Integer LOCKED = 4;
}
