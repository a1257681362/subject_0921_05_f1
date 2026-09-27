package com.evops.lens.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class LensRevokeRequest {

    @NotNull(message = "不能为空")
    private Long id;

    @NotNull(message = "不能为空")
    private Long versionNo;

    @Size(max = 255)
    private String reason;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getVersionNo() { return versionNo; }
    public void setVersionNo(Long versionNo) { this.versionNo = versionNo; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
