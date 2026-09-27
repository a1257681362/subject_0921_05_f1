package com.evops.order;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;

import java.math.BigDecimal;

/**
 * 处方（关联表）：加工单一左一右两条，随加工单同事务写入、同冲销同恢复。
 */
@TableName("t_prescription")
public class Prescription extends BaseEntity {

    private Long orderId;
    private String eye;          // L 左眼 / R 右眼
    private BigDecimal sphere;   // 球镜 S
    private BigDecimal cylinder; // 柱镜 C
    private Integer axis;        // 轴位 A
    private BigDecimal pd;       // 瞳距 mm

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
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
