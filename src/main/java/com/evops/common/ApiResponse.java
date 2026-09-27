package com.evops.common;

/**
 * 对外统一返回结构：业务码 + 提示 + 数据。
 */
public class ApiResponse<T> {

    private String code;
    private boolean success;
    private String message;
    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.code = BizCode.SUCCESS.getCode();
        response.success = true;
        response.message = BizCode.SUCCESS.getMessage();
        response.data = data;
        return response;
    }

    public static <T> ApiResponse<T> fail(BizCode bizCode) {
        return fail(bizCode.getCode(), bizCode.getMessage());
    }

    public static <T> ApiResponse<T> fail(BizCode bizCode, String message) {
        return fail(bizCode.getCode(), message);
    }

    public static <T> ApiResponse<T> fail(String code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.code = code;
        response.success = false;
        response.message = message;
        return response;
    }

    public String getCode() { return code; }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
}
