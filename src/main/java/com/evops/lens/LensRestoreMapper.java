package com.evops.lens;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * 冲销后记录对逻辑删除查询不可见，恢复/重复编码识别需要绕过 deleted 条件的原生 SQL。
 * 冲销、恢复均显式更新审计列，保证操作人/请求号/业务时间随每次写入留存。
 */
@Mapper
public interface LensRestoreMapper {

    @Select("SELECT * FROM t_lens WHERE id = #{id}")
    Lens selectByIdIncludeDeleted(@Param("id") Long id);

    @Select("SELECT * FROM t_lens WHERE lens_code = #{lensCode}")
    Lens selectByCodeIncludeDeleted(@Param("lensCode") String lensCode);

    @Update("UPDATE t_lens SET deleted = 1, version_no = version_no + 1, "
            + "operator = #{operator}, request_no = #{requestNo}, biz_time = #{bizTime}, "
            + "update_by = #{operator}, update_time = #{bizTime} "
            + "WHERE id = #{id} AND version_no = #{versionNo} AND deleted = 0")
    int revoke(@Param("id") Long id,
               @Param("versionNo") Long versionNo,
               @Param("operator") String operator,
               @Param("requestNo") String requestNo,
               @Param("bizTime") LocalDateTime bizTime);

    @Update("UPDATE t_lens SET deleted = 0, version_no = version_no + 1, "
            + "operator = #{operator}, request_no = #{requestNo}, biz_time = #{bizTime}, "
            + "update_by = #{operator}, update_time = #{bizTime} "
            + "WHERE id = #{id} AND version_no = #{versionNo} AND deleted = 1")
    int restore(@Param("id") Long id,
                @Param("versionNo") Long versionNo,
                @Param("operator") String operator,
                @Param("requestNo") String requestNo,
                @Param("bizTime") LocalDateTime bizTime);
}
