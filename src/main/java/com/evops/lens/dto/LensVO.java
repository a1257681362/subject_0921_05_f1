package com.evops.lens.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LensVO {

    private Long id;
    private String lensCode;
    private String lensName;
    private String brand;
    private String lensType;
    private BigDecimal unitPrice;
    private String remark;
    private String status;        // ACTIVE 生效 / REVOKED 已冲销
    private Long versionNo;
    private String operator;
    private String requestNo;
    private LocalDateTime bizTime;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLensCode() { return lensCode; }
    public void setLensCode(String lensCode) { this.lensCode = lensCode; }
    public String getLensName() { return lensName; }
    public void setLensName(String lensName) { this.lensName = lensName; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getLensType() { return lensType; }
    public void setLensType(String lensType) { this.lensType = lensType; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getVersionNo() { return versionNo; }
    public void setVersionNo(Long versionNo) { this.versionNo = versionNo; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public String getRequestNo() { return requestNo; }
    public void setRequestNo(String requestNo) { this.requestNo = requestNo; }
    public LocalDateTime getBizTime() { return bizTime; }
    public void setBizTime(LocalDateTime bizTime) { this.bizTime = bizTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
