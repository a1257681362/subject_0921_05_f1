package com.evops.order.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

public class OrderCreateRequest {

    /** 门店加工单号，客户端生成；重复提交由 uk_process_order_no 兜底。 */
    @NotBlank(message = "加工单号不能为空")
    @Size(max = 32, message = "加工单号长度不能超过32")
    private String orderNo;

    @NotBlank(message = "顾客姓名不能为空")
    @Size(max = 64)
    private String customerName;

    @Size(max = 32)
    private String customerPhone;

    @Size(max = 32)
    private String storeCode;

    private Long lensId;

    @Size(max = 255)
    private String remark;

    /** 左右眼处方，必须且只能包含 L、R 各一条，随加工单同事务生效。 */
    @Valid
    @NotEmpty(message = "左右眼处方不能为空")
    @Size(min = 2, max = 2, message = "必须且只能提交左右眼两条处方")
    private List<PrescriptionRequest> prescriptions;

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
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public List<PrescriptionRequest> getPrescriptions() { return prescriptions; }
    public void setPrescriptions(List<PrescriptionRequest> prescriptions) { this.prescriptions = prescriptions; }
}
