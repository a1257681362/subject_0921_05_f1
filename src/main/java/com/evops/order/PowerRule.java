package com.evops.order;

import com.evops.common.BizCode;
import com.evops.common.BizException;

import java.math.BigDecimal;

/**
 * 度数校验：25 度一档，即数值（D）必须是 0.25 的整数倍。
 * 如 -1.00、-3.75、+2.50 合法，-1.10 非法。
 */
public final class PowerRule {

    private static final BigDecimal STEP = new BigDecimal("0.25");
    private static final BigDecimal FOUR = new BigDecimal("4");

    private PowerRule() {
    }

    public static void checkPower(String fieldName, BigDecimal value) {
        if (value == null) {
            return;
        }
        BigDecimal quarters = value.multiply(FOUR);
        if (quarters.stripTrailingZeros().scale() > 0) {
            throw new BizException(BizCode.POWER_STEP_INVALID,
                    fieldName + " " + value.toPlainString() + " 不符合25度一档（0.25的整数倍）");
        }
    }

    public static BigDecimal step() {
        return STEP;
    }
}
