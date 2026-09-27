package com.evops.order.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 取镜状态流转 / 冲销恢复请求。
 * action：START_PROCESSING（开始加工）、READY（待取镜）、PICK（取镜完成）、CANCEL（冲销）；
 *         恢复走 /restore 入口，由服务端置为 RESTORE，故这里不强制非空。
 */
public class OrderTransitionRequest {

    @NotBlank(message = "加工单号不能为空")
    private String orderNo;

    private String action;

    @NotNull(message = "版本号不能为空")
    private Long versionNo;

    @Size(max = 255)
    private String remark;

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public Long getVersionNo() { return versionNo; }
    public void setVersionNo(Long versionNo) { this.versionNo = versionNo; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
