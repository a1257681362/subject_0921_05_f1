package com.evops.lens;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evops.common.BizCode;
import com.evops.common.BizException;
import com.evops.common.OperatorContext;
import com.evops.lens.dto.LensCreateRequest;
import com.evops.lens.dto.LensRevokeRequest;
import com.evops.lens.dto.LensUpdateRequest;
import com.evops.lens.dto.LensVO;
import com.evops.oplog.OpLogService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class LensService {

    private static final String BIZ_TYPE = "LENS";

    @Resource
    private LensMapper lensMapper;
    @Resource
    private LensRestoreMapper lensRestoreMapper;
    @Resource
    private OpLogService opLogService;

    @Transactional(rollbackFor = Exception.class)
    public LensVO create(LensCreateRequest request) {
        Lens revoked = lensRestoreMapper.selectByCodeIncludeDeleted(request.getLensCode());
        if (revoked != null) {
            throw new BizException(BizCode.LENS_DUPLICATE,
                    revoked.getDeleted() == 1 ? "镜片编码已存在（该编码已冲销，请先恢复后再用）"
                            : "镜片编码已存在");
        }

        Lens lens = new Lens();
        lens.setLensCode(request.getLensCode());
        lens.setLensName(request.getLensName());
        lens.setBrand(request.getBrand());
        lens.setLensType(request.getLensType());
        lens.setUnitPrice(request.getUnitPrice());
        lens.setRemark(request.getRemark());
        try {
            lensMapper.insert(lens);
        } catch (DuplicateKeyException ex) {
            // 并发重复建档由库层唯一约束 uk_lens_code 兜底
            throw new BizException(BizCode.LENS_DUPLICATE);
        }
        opLogService.record(BIZ_TYPE, lens.getId(), lens.getLensCode(), "CREATE",
                null, "ACTIVE", "镜片建档", lens.getVersionNo());
        return toVO(lens, "ACTIVE");
    }

    @Transactional(rollbackFor = Exception.class)
    public LensVO update(LensUpdateRequest request) {
        Lens lens = lensMapper.selectById(request.getId());
        if (lens == null) {
            throw new BizException(BizCode.LENS_NOT_FOUND);
        }
        lens.setLensName(request.getLensName());
        if (request.getBrand() != null) {
            lens.setBrand(request.getBrand());
        }
        if (request.getLensType() != null) {
            lens.setLensType(request.getLensType());
        }
        if (request.getUnitPrice() != null) {
            lens.setUnitPrice(request.getUnitPrice());
        }
        if (request.getRemark() != null) {
            lens.setRemark(request.getRemark());
        }
        lens.setVersionNo(request.getVersionNo());

        int rows;
        try {
            rows = lensMapper.updateById(lens);
        } catch (DuplicateKeyException ex) {
            throw new BizException(BizCode.LENS_DUPLICATE);
        }
        if (rows == 0) {
            throw new BizException(BizCode.VERSION_CONFLICT);
        }
        // 乐观锁插件更新成功后会把新版本号写回实体
        opLogService.record(BIZ_TYPE, lens.getId(), lens.getLensCode(), "UPDATE",
                "ACTIVE", "ACTIVE", "镜片信息修改", lens.getVersionNo());
        return getById(lens.getId());
    }

    /** 冲销：逻辑删除，留操作记录。 */
    @Transactional(rollbackFor = Exception.class)
    public LensVO revoke(LensRevokeRequest request) {
        Lens active = lensMapper.selectById(request.getId());
        if (active == null) {
            Lens any = lensRestoreMapper.selectByIdIncludeDeleted(request.getId());
            if (any == null) {
                throw new BizException(BizCode.LENS_NOT_FOUND);
            }
            throw new BizException(BizCode.LENS_STATUS_INVALID, "镜片已冲销，请勿重复冲销");
        }
        OperatorContext.OpInfo op = OperatorContext.current();
        int rows = lensRestoreMapper.revoke(active.getId(), request.getVersionNo(),
                op.getOperator(), op.getRequestNo(), op.getBizTime());
        if (rows == 0) {
            throw new BizException(BizCode.VERSION_CONFLICT);
        }
        opLogService.record(BIZ_TYPE, active.getId(), active.getLensCode(), "REVOKE",
                "ACTIVE", "REVOKED",
                request.getReason() == null ? "镜片冲销" : "镜片冲销：" + request.getReason(),
                active.getVersionNo() + 1);
        return getByIdIncludeDeleted(active.getId());
    }

    /** 恢复：逻辑删除标志置回，留操作记录。 */
    @Transactional(rollbackFor = Exception.class)
    public LensVO restore(LensRevokeRequest request) {
        Lens lens = lensRestoreMapper.selectByIdIncludeDeleted(request.getId());
        if (lens == null) {
            throw new BizException(BizCode.LENS_NOT_FOUND);
        }
        if (lens.getDeleted() == 0) {
            throw new BizException(BizCode.LENS_STATUS_INVALID, "镜片当前为生效状态，无需恢复");
        }
        OperatorContext.OpInfo op = OperatorContext.current();
        int rows = lensRestoreMapper.restore(lens.getId(), request.getVersionNo(),
                op.getOperator(), op.getRequestNo(), op.getBizTime());
        if (rows == 0) {
            throw new BizException(BizCode.VERSION_CONFLICT);
        }
        opLogService.record(BIZ_TYPE, lens.getId(), lens.getLensCode(), "RESTORE",
                "REVOKED", "ACTIVE", "镜片恢复", lens.getVersionNo() + 1);
        return getById(lens.getId());
    }

    public LensVO getById(Long id) {
        Lens lens = lensMapper.selectById(id);
        if (lens == null) {
            throw new BizException(BizCode.LENS_NOT_FOUND);
        }
        return toVO(lens, "ACTIVE");
    }

    public LensVO getByIdIncludeDeleted(Long id) {
        Lens lens = lensRestoreMapper.selectByIdIncludeDeleted(id);
        if (lens == null) {
            throw new BizException(BizCode.LENS_NOT_FOUND);
        }
        return toVO(lens, lens.getDeleted() == 1 ? "REVOKED" : "ACTIVE");
    }

    public Page<LensVO> page(long current, long size, String keyword) {
        LambdaQueryWrapper<Lens> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(Lens::getLensCode, kw).or().like(Lens::getLensName, kw));
        }
        // 已冲销记录默认不出现在业务检索中（逻辑删除条件由 MyBatis-Plus 自动追加）
        wrapper.orderByDesc(Lens::getId);
        Page<Lens> page = lensMapper.selectPage(new Page<>(current, size), wrapper);
        Page<LensVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        page.getRecords().forEach(lens -> result.getRecords().add(toVO(lens, "ACTIVE")));
        return result;
    }

    private LensVO toVO(Lens lens, String status) {
        LensVO vo = new LensVO();
        vo.setId(lens.getId());
        vo.setLensCode(lens.getLensCode());
        vo.setLensName(lens.getLensName());
        vo.setBrand(lens.getBrand());
        vo.setLensType(lens.getLensType());
        vo.setUnitPrice(lens.getUnitPrice());
        vo.setRemark(lens.getRemark());
        vo.setStatus(status);
        vo.setVersionNo(lens.getVersionNo());
        vo.setOperator(lens.getOperator());
        vo.setRequestNo(lens.getRequestNo());
        vo.setBizTime(lens.getBizTime());
        vo.setUpdateTime(lens.getUpdateTime());
        return vo;
    }
}
