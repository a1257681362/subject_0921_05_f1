package com.evops.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PrescriptionVO {

    private Long id;
    private String eye;
    private BigDecimal sphere;
    private BigDecimal cylinder;
    private Integer axis;
    private BigDecimal pd;
    private Long versionNo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public Long getVersionNo() { return versionNo; }
    public void setVersionNo(Long versionNo) { this.versionNo = versionNo; }
}
