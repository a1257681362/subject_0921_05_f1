package com.evops.common;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * 从请求头解析操作人 / 请求号 / 业务时间，写入线程上下文。
 */
@Component
public class OperatorInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String operator = header(request, "X-Operator", "system");
        String requestNo = header(request, "X-Request-No", null);
        String bizTimeText = header(request, "X-Biz-Time", null);

        LocalDateTime bizTime = LocalDateTime.now();
        if (bizTimeText != null && !bizTimeText.trim().isEmpty()) {
            try {
                bizTime = LocalDateTime.parse(bizTimeText.replace(' ', 'T'));
            } catch (DateTimeParseException ex) {
                throw new BizException(BizCode.PARAM_INVALID, "业务时间格式应为 yyyy-MM-ddTHH:mm:ss");
            }
        }
        OperatorContext.set(operator, requestNo, bizTime);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        OperatorContext.clear();
    }

    private String header(HttpServletRequest request, String name, String defaultValue) {
        String value = request.getHeader(name);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }
}
