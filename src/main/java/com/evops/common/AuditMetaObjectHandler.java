package com.evops.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 写入时自动填充审计列。
 */
@Component
public class AuditMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        OperatorContext.OpInfo op = OperatorContext.current();
        LocalDateTime now = op.getBizTime();
        String operator = op.getOperator();

        strictInsertFill(metaObject, "deleted", Integer.class, 0);
        strictInsertFill(metaObject, "versionNo", Long.class, 0L);
        strictInsertFill(metaObject, "operator", String.class, operator);
        strictInsertFill(metaObject, "requestNo", String.class, op.getRequestNo());
        strictInsertFill(metaObject, "bizTime", LocalDateTime.class, now);
        strictInsertFill(metaObject, "createBy", String.class, operator);
        strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        strictInsertFill(metaObject, "updateBy", String.class, operator);
        strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        OperatorContext.OpInfo op = OperatorContext.current();
        LocalDateTime now = op.getBizTime();
        String operator = op.getOperator();

        strictUpdateFill(metaObject, "operator", String.class, operator);
        strictUpdateFill(metaObject, "requestNo", String.class, op.getRequestNo());
        strictUpdateFill(metaObject, "bizTime", LocalDateTime.class, now);
        strictUpdateFill(metaObject, "updateBy", String.class, operator);
        strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, now);
    }
}
