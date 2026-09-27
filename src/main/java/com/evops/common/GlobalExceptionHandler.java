package com.evops.common;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ApiResponse<Void> handleBiz(BizException ex) {
        String message = ex.getMessage() == null || ex.getMessage().isEmpty()
                ? ex.getBizCode().getMessage()
                : ex.getMessage();
        return ApiResponse.fail(ex.getBizCode(), message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleNotValid(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        return ApiResponse.fail(BizCode.PARAM_INVALID, detail);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<Void> handleConstraint(ConstraintViolationException ex) {
        return ApiResponse.fail(BizCode.PARAM_INVALID, ex.getMessage());
    }

    /**
     * 兜底：重复提交应由业务层根据唯一约束翻译成明确业务码，
     * 走到这里说明约束归属未识别，按系统错误返回并保留日志线索。
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public ApiResponse<Void> handleDuplicate(DuplicateKeyException ex) {
        Throwable cause = ex.getMostSpecificCause();
        return ApiResponse.fail(BizCode.SYSTEM_ERROR,
                "数据唯一性冲突：" + (cause == null ? ex.getMessage() : cause.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<Void> handleIllegalArgument(IllegalArgumentException ex) {
        return ApiResponse.fail(BizCode.PARAM_INVALID, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleUnexpected(Exception ex) {
        return ApiResponse.fail(BizCode.SYSTEM_ERROR);
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + " " + error.getDefaultMessage();
    }
}
