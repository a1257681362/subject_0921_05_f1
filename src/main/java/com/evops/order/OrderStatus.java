package com.evops.order;

/**
 * 加工单状态与操作类型常量。
 */
public final class OrderStatus {

    public static final String CREATED = "CREATED";
    public static final String PROCESSING = "PROCESSING";
    public static final String READY = "READY";
    public static final String PICKED = "PICKED";
    public static final String CANCELLED = "CANCELLED";

    public static final String ACT_START_PROCESSING = "START_PROCESSING";
    public static final String ACT_READY = "READY";
    public static final String ACT_PICK = "PICK";
    public static final String ACT_CANCEL = "CANCEL";
    public static final String ACT_RESTORE = "RESTORE";

    private OrderStatus() {
    }
}
