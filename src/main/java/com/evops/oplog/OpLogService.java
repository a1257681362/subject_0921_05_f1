package com.evops.oplog;

import com.evops.common.OperatorContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 操作留痕统一入口。
 */
@Service
public class OpLogService {

    @Resource
    private OpLogMapper opLogMapper;

    public void record(String bizType, Long bizId, String bizNo, String action,
                       String beforeStatus, String afterStatus, String detail, Long versionNo) {
        OperatorContext.OpInfo op = OperatorContext.current();
        OpLog log = new OpLog();
        log.setBizType(bizType);
        log.setBizId(bizId);
        log.setBizNo(bizNo);
        log.setAction(action);
        log.setBeforeStatus(beforeStatus);
        log.setAfterStatus(afterStatus);
        log.setDetail(detail);
        log.setOperator(op.getOperator());
        log.setRequestNo(op.getRequestNo());
        log.setBizTime(op.getBizTime());
        log.setVersionNo(versionNo);
        opLogMapper.insert(log);
    }
}
