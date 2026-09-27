package com.evops.common;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 操作上下文：操作人、请求号、业务时间，随每次写入留存。
 * 由 {@code OperatorInterceptor} 从请求头解析：
 * X-Operator / X-Request-No / X-Biz-Time(yyyy-MM-ddTHH:mm:ss)。
 * 非 HTTP 场景（定时任务、测试）使用默认值，保证写入字段不为空。
 */
public final class OperatorContext {

    private static final ThreadLocal<OpInfo> HOLDER = new ThreadLocal<>();

    private OperatorContext() {
    }

    public static void set(String operator, String requestNo, LocalDateTime bizTime) {
        HOLDER.set(new OpInfo(operator, requestNo, bizTime));
    }

    public static OpInfo current() {
        OpInfo info = HOLDER.get();
        if (info == null) {
            info = new OpInfo("system", null, LocalDateTime.now());
        }
        if (info.getRequestNo() == null || info.getRequestNo().trim().isEmpty()) {
            info = new OpInfo(info.getOperator(),
                    "AUTO-" + UUID.randomUUID().toString().replace("-", ""),
                    info.getBizTime());
            HOLDER.set(info);
        }
        return info;
    }

    public static void clear() {
        HOLDER.remove();
    }

    public static class OpInfo {
        private final String operator;
        private final String requestNo;
        private final LocalDateTime bizTime;

        public OpInfo(String operator, String requestNo, LocalDateTime bizTime) {
            this.operator = operator;
            this.requestNo = requestNo;
            this.bizTime = bizTime == null ? LocalDateTime.now() : bizTime;
        }

        public String getOperator() { return operator; }
        public String getRequestNo() { return requestNo; }
        public LocalDateTime getBizTime() { return bizTime; }
    }
}
