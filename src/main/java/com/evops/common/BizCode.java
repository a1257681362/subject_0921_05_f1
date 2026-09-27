package com.evops.common;

/**
 * 统一业务码。HTTP 状态码一律 200，成败与原因看这里。
 */
public enum BizCode {

    SUCCESS("0", "成功"),
    PARAM_INVALID("A0400", "请求参数有误"),
    VERSION_CONFLICT("B1009", "数据已被他人修改，请刷新后重试"),

    LENS_DUPLICATE("B1101", "镜片编码已存在"),
    LENS_NOT_FOUND("B1102", "镜片不存在"),
    LENS_STATUS_INVALID("B1103", "镜片当前状态不允许该操作"),

    ORDER_DUPLICATE("B1201", "加工单号已存在，请勿重复提交"),
    ORDER_NOT_FOUND("B1202", "加工单不存在"),
    ORDER_STATUS_INVALID("B1203", "加工单当前状态不允许该操作"),
    POWER_STEP_INVALID("B1204", "度数必须按25度一档（0.25的整数倍）"),

    SYSTEM_ERROR("B9999", "系统繁忙，请稍后再试");

    private final String code;
    private final String message;

    BizCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
