package com.evops.order;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;

/**
 * 加工单主表。
 * 状态机（取镜流转主路径）：
 * CREATED 已建档 → PROCESSING 加工中 → READY 待取镜 → PICKED 已取镜；
 * CREATED → CANCELLED 已冲销（逻辑删除），CANCELLED → 恢复回 CREATED。
 */
@TableName("t_process_order")
public class ProcessOrder extends BaseEntity {

    private String orderNo;
    private String customerName;
    private String customerPhone;
    private String storeCode;
    private Long lensId;
    private String status;
    private String remark;

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
}
