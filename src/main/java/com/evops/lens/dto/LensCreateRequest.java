package com.evops.lens.dto;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

public class LensCreateRequest {

    @NotBlank(message = "不能为空")
    @Size(max = 32, message = "长度不能超过32")
    private String lensCode;

    @NotBlank(message = "不能为空")
    @Size(max = 64, message = "长度不能超过64")
    private String lensName;

    @Size(max = 64)
    private String brand;

    @Size(max = 32)
    private String lensType;

    @NotNull(message = "不能为空")
    @DecimalMin(value = "0.00", message = "不能为负")
    private BigDecimal unitPrice;

    @Size(max = 255)
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
