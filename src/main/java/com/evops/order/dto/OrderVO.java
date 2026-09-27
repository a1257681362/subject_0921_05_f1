package com.evops.order.dto;

import java.time.LocalDateTime;
import java.util.List;

public class OrderVO {

    private Long id;
    private String orderNo;
    private String customerName;
    private String customerPhone;
    private String storeCode;
    private Long lensId;
    private String status;
    private String remark;
    private Long versionNo;
    private String operator;
    private String requestNo;
    private LocalDateTime bizTime;
    private LocalDateTime updateTime;
    private List<PrescriptionVO> prescriptions;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public String getStoreCode() { return storeCode; }
    public void setStoreCode(String storeCode) { this.storeCode = storeCode; }
    public Long getLensId() { return lensId; }
    public void setLensId(Long lensId) { this.lensId = lensId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
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
    public List<PrescriptionVO> getPrescriptions() { return prescriptions; }
    public void setPrescriptions(List<PrescriptionVO> prescriptions) { this.prescriptions = prescriptions; }
}
