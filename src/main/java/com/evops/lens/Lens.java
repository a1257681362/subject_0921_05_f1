package com.evops.lens;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;

import java.math.BigDecimal;

/**
 * 镜片基础信息。
 */
@TableName("t_lens")
public class Lens extends BaseEntity {

    private String lensCode;
    private String lensName;
    private String brand;
    private String lensType;
    private BigDecimal unitPrice;
    private String remark;

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
}
