package com.evops.order;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.evops.common.BizCode;
import com.evops.common.BizException;
import com.evops.common.OperatorContext;
import com.evops.lens.LensMapper;
import com.evops.oplog.OpLogService;
import com.evops.order.dto.OrderCreateRequest;
import com.evops.order.dto.OrderTransitionRequest;
import com.evops.order.dto.OrderVO;
import com.evops.order.dto.PrescriptionRequest;
import com.evops.order.dto.PrescriptionVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProcessOrderService {

    private static final String BIZ_TYPE = "PROCESS_ORDER";

    @Resource
    private ProcessOrderMapper orderMapper;
    @Resource
    private PrescriptionMapper prescriptionMapper;
    @Resource
    private OrderRestoreMapper restoreMapper;
    @Resource
    private LensMapper lensMapper;
    @Resource
    private OpLogService opLogService;

    /**
     * 加工单建档：主表 + 左右眼处方同事务，要么全部生效要么全部不见。
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderVO create(OrderCreateRequest request) {
        validatePrescriptions(request.getPrescriptions());
        if (request.getLensId() != null && lensMapper.selectById(request.getLensId()) == null) {
            throw new BizException(BizCode.LENS_NOT_FOUND, "选用的镜片不存在或已冲销");
        }
        // 已冲销的单号也不允许复用，避免与历史底账混淆
        ProcessOrder exists = restoreMapper.selectByNoIncludeDeleted(request.getOrderNo());
        if (exists != null) {
            throw exists.getDeleted() == 1
                    ? new BizException(BizCode.ORDER_DUPLICATE, "加工单号已存在（该单已冲销），请勿重复提交")
                    : new BizException(BizCode.ORDER_DUPLICATE);
        }

        ProcessOrder order = new ProcessOrder();
        order.setOrderNo(request.getOrderNo());
        order.setCustomerName(request.getCustomerName());
        order.setCustomerPhone(request.getCustomerPhone());
        order.setStoreCode(request.getStoreCode());
        order.setLensId(request.getLensId());
        order.setStatus(OrderStatus.CREATED);
        order.setRemark(request.getRemark());
        try {
            orderMapper.insert(order);
        } catch (DuplicateKeyException ex) {
            // 并发重复提交由库层唯一约束 uk_process_order_no 兜底
            throw new BizException(BizCode.ORDER_DUPLICATE);
        }

        try {
            for (PrescriptionRequest pr : request.getPrescriptions()) {
                Prescription entity = new Prescription();
                entity.setOrderId(order.getId());
                entity.setEye(pr.getEye().toUpperCase());
                entity.setSphere(pr.getSphere());
                entity.setCylinder(pr.getCylinder());
                entity.setAxis(pr.getAxis());
                entity.setPd(pr.getPd());
                prescriptionMapper.insert(entity);
            }
        } catch (DuplicateKeyException ex) {
            throw new BizException(BizCode.ORDER_DUPLICATE, "处方信息重复提交");
        }

        opLogService.record(BIZ_TYPE, order.getId(), order.getOrderNo(), "CREATE",
                null, OrderStatus.CREATED, "加工单建档", order.getVersionNo());
        return getByNo(order.getOrderNo());
    }

    /** 取镜状态流转主路径 + 冲销恢复。 */
    @Transactional(rollbackFor = Exception.class)
    public OrderVO transition(OrderTransitionRequest request) {
        ProcessOrder order = orderMapper.selectOne(
                new LambdaQueryWrapper<ProcessOrder>().eq(ProcessOrder::getOrderNo, request.getOrderNo()));
        if (order == null) {
            ProcessOrder any = restoreMapper.selectByNoIncludeDeleted(request.getOrderNo());
            if (any == null) {
                throw new BizException(BizCode.ORDER_NOT_FOUND);
            }
            throw new BizException(BizCode.ORDER_STATUS_INVALID, "加工单已冲销，请先恢复");
        }

        OperatorContext.OpInfo op = OperatorContext.current();
        String action = request.getAction();
        String from = order.getStatus();
        String to;

        switch (action) {
            case OrderStatus.ACT_START_PROCESSING:
                to = OrderStatus.PROCESSING;
                requireFrom(from, OrderStatus.CREATED);
                break;
            case OrderStatus.ACT_READY:
                to = OrderStatus.READY;
                requireFrom(from, OrderStatus.PROCESSING);
                break;
            case OrderStatus.ACT_PICK:
                to = OrderStatus.PICKED;
                requireFrom(from, OrderStatus.READY);
                break;
            case OrderStatus.ACT_CANCEL:
                return cancel(order, request);
            default:
                throw new BizException(BizCode.PARAM_INVALID, "未知操作类型：" + action);
        }

        int rows = restoreMapper.changeStatus(order.getId(), request.getVersionNo(),
                from, to, op.getOperator(), op.getRequestNo(), op.getBizTime());
        if (rows == 0) {
            throw new BizException(BizCode.VERSION_CONFLICT);
        }
        opLogService.record(BIZ_TYPE, order.getId(), order.getOrderNo(), "STATUS_CHANGE",
                from, to, request.getRemark(), request.getVersionNo() + 1);
        return getByNo(order.getOrderNo());
    }

    /** 冲销：仅允许已建档未加工的单据，主表与处方同时逻辑删除。 */
    @Transactional(rollbackFor = Exception.class)
    public OrderVO cancel(ProcessOrder order, OrderTransitionRequest request) {
        requireFrom(order.getStatus(), OrderStatus.CREATED);
        OperatorContext.OpInfo op = OperatorContext.current();
        int rows = restoreMapper.cancel(order.getId(), request.getVersionNo(),
                op.getOperator(), op.getRequestNo(), op.getBizTime());
        if (rows == 0) {
            throw new BizException(BizCode.VERSION_CONFLICT);
        }
        // 关联处方随单冲销（全部生效或全部不见）
        int prescriptionRows = restoreMapper.syncPrescriptionDeleted(order.getId(),
                1, 0, op.getOperator(), op.getRequestNo(), op.getBizTime());
        opLogService.record(BIZ_TYPE, order.getId(), order.getOrderNo(), "REVOKE",
                OrderStatus.CREATED, OrderStatus.CANCELLED,
                request.getRemark() == null ? "加工单冲销" : "加工单冲销：" + request.getRemark(),
                request.getVersionNo() + 1);
        if (prescriptionRows == 0) {
            throw new BizException(BizCode.SYSTEM_ERROR, "关联处方冲销失败，事务回滚");
        }
        return getByNoIncludeDeleted(order.getOrderNo());
    }

    /** 恢复：主表与处方同时恢复。 */
    @Transactional(rollbackFor = Exception.class)
    public OrderVO restore(OrderTransitionRequest request) {
        ProcessOrder order = restoreMapper.selectByNoIncludeDeleted(request.getOrderNo());
        if (order == null) {
            throw new BizException(BizCode.ORDER_NOT_FOUND);
        }
        if (order.getDeleted() == 0) {
            throw new BizException(BizCode.ORDER_STATUS_INVALID, "加工单当前为生效状态，无需恢复");
        }
        OperatorContext.OpInfo op = OperatorContext.current();
        int rows = restoreMapper.restore(order.getId(), request.getVersionNo(),
                op.getOperator(), op.getRequestNo(), op.getBizTime());
        if (rows == 0) {
            throw new BizException(BizCode.VERSION_CONFLICT);
        }
        int prescriptionRows = restoreMapper.syncPrescriptionDeleted(order.getId(),
                0, 1, op.getOperator(), op.getRequestNo(), op.getBizTime());
        if (prescriptionRows == 0) {
            throw new BizException(BizCode.SYSTEM_ERROR, "关联处方恢复失败，事务回滚");
        }
        opLogService.record(BIZ_TYPE, order.getId(), order.getOrderNo(), "RESTORE",
                OrderStatus.CANCELLED, OrderStatus.CREATED, "加工单恢复", request.getVersionNo() + 1);
        return getByNo(order.getOrderNo());
    }

    /** 按加工单号检索（主路径只返回生效单据，返回左右眼处方）。 */
    public OrderVO getByNo(String orderNo) {
        ProcessOrder order = orderMapper.selectOne(
                new LambdaQueryWrapper<ProcessOrder>().eq(ProcessOrder::getOrderNo, orderNo));
        if (order == null) {
            throw new BizException(BizCode.ORDER_NOT_FOUND);
        }
        return toVO(order, loadPrescriptions(order.getId()));
    }

    private OrderVO getByNoIncludeDeleted(String orderNo) {
        ProcessOrder order = restoreMapper.selectByNoIncludeDeleted(orderNo);
        List<Prescription> prescriptions = restoreMapper.selectPrescriptionsIncludeDeleted(order.getId());
        return toVO(order, prescriptions);
    }

    private List<Prescription> loadPrescriptions(Long orderId) {
        return prescriptionMapper.selectList(
                new LambdaQueryWrapper<Prescription>().eq(Prescription::getOrderId, orderId));
    }

    private void validatePrescriptions(List<PrescriptionRequest> prescriptions) {
        Set<String> eyes = new HashSet<>();
        for (PrescriptionRequest pr : prescriptions) {
            String eye = pr.getEye() == null ? "" : pr.getEye().toUpperCase();
            if (!"L".equals(eye) && !"R".equals(eye)) {
                throw new BizException(BizCode.PARAM_INVALID, "眼别只能是 L（左眼）或 R（右眼）");
            }
            if (!eyes.add(eye)) {
                throw new BizException(BizCode.PARAM_INVALID, "左右眼处方各只能提交一条，重复眼别：" + eye);
            }
            PowerRule.checkPower(eye + "眼球镜S", pr.getSphere());
            PowerRule.checkPower(eye + "眼柱镜C", pr.getCylinder());
        }
        if (!eyes.equals(new HashSet<>(Arrays.asList("L", "R")))) {
            throw new BizException(BizCode.PARAM_INVALID, "必须同时提交左眼 L 与右眼 R 处方");
        }
    }

    private void requireFrom(String actual, String expected) {
        if (!expected.equals(actual)) {
            throw new BizException(BizCode.ORDER_STATUS_INVALID,
                    "当前状态为 " + actual + "，不允许该操作（要求状态 " + expected + "）");
        }
    }

    private OrderVO toVO(ProcessOrder order, List<Prescription> prescriptions) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setCustomerName(order.getCustomerName());
        vo.setCustomerPhone(order.getCustomerPhone());
        vo.setStoreCode(order.getStoreCode());
        vo.setLensId(order.getLensId());
        vo.setStatus(order.getStatus());
        vo.setRemark(order.getRemark());
        vo.setVersionNo(order.getVersionNo());
        vo.setOperator(order.getOperator());
        vo.setRequestNo(order.getRequestNo());
        vo.setBizTime(order.getBizTime());
        vo.setUpdateTime(order.getUpdateTime());

        List<PrescriptionVO> pvos = new ArrayList<>();
        for (Prescription p : prescriptions) {
            PrescriptionVO pvo = new PrescriptionVO();
            pvo.setId(p.getId());
            pvo.setEye(p.getEye());
            pvo.setSphere(p.getSphere());
            pvo.setCylinder(p.getCylinder());
            pvo.setAxis(p.getAxis());
            pvo.setPd(p.getPd());
            pvo.setVersionNo(p.getVersionNo());
            pvos.add(pvo);
        }
        vo.setPrescriptions(pvos);
        return vo;
    }
}
