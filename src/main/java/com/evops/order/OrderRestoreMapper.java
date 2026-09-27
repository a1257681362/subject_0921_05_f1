package com.evops.order;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * 冲销后单据及处方对逻辑删除查询不可见，恢复/单号重复识别需要绕过 deleted 条件。
 * 冲销、恢复同时处理主表与处方表，并显式更新审计列。
 */
@Mapper
public interface OrderRestoreMapper {

    @Select("SELECT * FROM t_process_order WHERE order_no = #{orderNo}")
    ProcessOrder selectByNoIncludeDeleted(@Param("orderNo") String orderNo);

    @Select("SELECT * FROM t_prescription WHERE order_id = #{orderId}")
    java.util.List<Prescription> selectPrescriptionsIncludeDeleted(@Param("orderId") Long orderId);

    /**
     * 取镜状态流转：仅当当前状态=fromStatus 且版本号匹配时生效（状态机 + 乐观锁）。
     */
    @Update("UPDATE t_process_order SET status = #{toStatus}, version_no = version_no + 1, "
            + "operator = #{operator}, request_no = #{requestNo}, biz_time = #{bizTime}, "
            + "update_by = #{operator}, update_time = #{bizTime} "
            + "WHERE id = #{id} AND version_no = #{versionNo} AND deleted = 0 AND status = #{fromStatus}")
    int changeStatus(@Param("id") Long id,
                     @Param("versionNo") Long versionNo,
                     @Param("fromStatus") String fromStatus,
                     @Param("toStatus") String toStatus,
                     @Param("operator") String operator,
                     @Param("requestNo") String requestNo,
                     @Param("bizTime") LocalDateTime bizTime);

    @Update("UPDATE t_process_order SET deleted = 1, status = 'CANCELLED', "
            + "version_no = version_no + 1, operator = #{operator}, request_no = #{requestNo}, "
            + "biz_time = #{bizTime}, update_by = #{operator}, update_time = #{bizTime} "
            + "WHERE id = #{id} AND version_no = #{versionNo} AND deleted = 0 AND status = 'CREATED'")
    int cancel(@Param("id") Long id,
               @Param("versionNo") Long versionNo,
               @Param("operator") String operator,
               @Param("requestNo") String requestNo,
               @Param("bizTime") LocalDateTime bizTime);

    @Update("UPDATE t_process_order SET deleted = 0, status = 'CREATED', "
            + "version_no = version_no + 1, operator = #{operator}, request_no = #{requestNo}, "
            + "biz_time = #{bizTime}, update_by = #{operator}, update_time = #{bizTime} "
            + "WHERE id = #{id} AND version_no = #{versionNo} AND deleted = 1 AND status = 'CANCELLED'")
    int restore(@Param("id") Long id,
                @Param("versionNo") Long versionNo,
                @Param("operator") String operator,
                @Param("requestNo") String requestNo,
                @Param("bizTime") LocalDateTime bizTime);

    @Update("UPDATE t_prescription SET deleted = #{deleted}, version_no = version_no + 1, "
            + "operator = #{operator}, request_no = #{requestNo}, biz_time = #{bizTime}, "
            + "update_by = #{operator}, update_time = #{bizTime} "
            + "WHERE order_id = #{orderId} AND deleted = #{oppositeDeleted}")
    int syncPrescriptionDeleted(@Param("orderId") Long orderId,
                                @Param("deleted") int deleted,
                                @Param("oppositeDeleted") int oppositeDeleted,
                                @Param("operator") String operator,
                                @Param("requestNo") String requestNo,
                                @Param("bizTime") LocalDateTime bizTime);
}
