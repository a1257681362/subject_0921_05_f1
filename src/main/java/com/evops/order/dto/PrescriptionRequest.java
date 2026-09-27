package com.evops.order.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 单眼处方。sphere / cylinder 单位为 D（度），由业务层按 25 度一档校验；
 * axis 轴位 1~180；pd 瞳距单位 mm。
 */
public class PrescriptionRequest {

    @NotNull(message = "眼别不能为空（L 左眼 / R 右眼）")
    private String eye;

    private BigDecimal sphere;
    private BigDecimal cylinder;

    @Min(value = 1, message = "轴位应在1~180之间")
    @Max(value = 180, message = "轴位应在1~180之间")
    private Integer axis;

    @Min(value = 20, message = "瞳距数值不合理")
    @Max(value = 80, message = "瞳距数值不合理")
    private BigDecimal pd;

    public String getEye() { return eye; }
    public void setEye(String eye) { this.eye = eye; }
    public BigDecimal getSphere() { return sphere; }
    public void setSphere(BigDecimal sphere) { this.sphere = sphere; }
    public BigDecimal getCylinder() { return cylinder; }
    public void setCylinder(BigDecimal cylinder) { this.cylinder = cylinder; }
    public Integer getAxis() { return axis; }
    public void setAxis(Integer axis) { this.axis = axis; }
    public BigDecimal getPd() { return pd; }
    public void setPd(BigDecimal pd) { this.pd = pd; }
}
