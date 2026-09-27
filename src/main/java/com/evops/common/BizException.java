package com.evops.common;

/**
 * 业务异常：携带明确业务码，由全局异常处理统一转成返回结构。
 */
public class BizException extends RuntimeException {

    private final BizCode bizCode;

    public BizException(BizCode bizCode) {
        super(bizCode.getMessage());
        this.bizCode = bizCode;
    }

    public BizException(BizCode bizCode, String detail) {
        super(detail);
        this.bizCode = bizCode;
    }

    public BizCode getBizCode() {
        return bizCode;
    }
}
