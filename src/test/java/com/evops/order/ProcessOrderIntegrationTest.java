package com.evops.order;

import com.evops.common.BizCode;
import com.evops.common.BizException;
import com.evops.common.OperatorContext;
import com.evops.order.dto.OrderCreateRequest;
import com.evops.order.dto.OrderTransitionRequest;
import com.evops.order.dto.OrderVO;
import com.evops.order.dto.PrescriptionRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:order-test;DB_CLOSE_DELAY=-1",
        "spring.sql.init.mode=always"
})
class ProcessOrderIntegrationTest {

    @Autowired
    private ProcessOrderService orderService;

    @BeforeEach
    void setUp() {
        OperatorContext.set("op-zhang", "REQ-ORDER-0001", LocalDateTime.of(2026, 9, 26, 11, 0));
    }

    @Test
    void create_validPower_persistsBothEyes() {
        OrderVO vo = orderService.create(buildCreate("PO-2001", "-3.75", "-1.00", "-3.50", "-0.75"));

        Assertions.assertEquals(OrderStatus.CREATED, vo.getStatus());
        Assertions.assertEquals(2, vo.getPrescriptions().size());
        Assertions.assertEquals("op-zhang", vo.getOperator());
        Assertions.assertEquals("REQ-ORDER-0001", vo.getRequestNo());
    }

    @Test
    void create_powerNotOnStep_rejected() {
        // -3.80 不是 0.25 的整数倍
        BizException ex = Assertions.assertThrows(BizException.class,
                () -> orderService.create(buildCreate("PO-2002", "-3.80", "-1.00", "-3.50", "-0.75")));
        Assertions.assertEquals(BizCode.POWER_STEP_INVALID.getCode(), ex.getBizCode().getCode());
    }

    @Test
    void create_missingEye_rejected() {
        OrderCreateRequest req = buildCreate("PO-2003", "-3.75", "-1.00", "-3.50", "-0.75");
        req.setPrescriptions(new java.util.ArrayList<>(req.getPrescriptions()));
        req.getPrescriptions().removeIf(p -> "R".equals(p.getEye()));
        Assertions.assertThrows(BizException.class, () -> orderService.create(req));
    }

    @Test
    void create_duplicateOrderNo_returnsOrderDuplicate() {
        orderService.create(buildCreate("PO-2004", "-3.75", "-1.00", "-3.50", "-0.75"));
        BizException ex = Assertions.assertThrows(BizException.class,
                () -> orderService.create(buildCreate("PO-2004", "-2.00", "-1.00", "-1.75", "-0.75")));
        Assertions.assertEquals(BizCode.ORDER_DUPLICATE.getCode(), ex.getBizCode().getCode());
    }

    @Test
    void fullPickupFlow_createdToPicked() {
        OrderVO vo = orderService.create(buildCreate("PO-2005", "-3.75", "-1.00", "-3.50", "-0.75"));
        long version = vo.getVersionNo();

        vo = orderService.transition(transition("PO-2005", OrderStatus.ACT_START_PROCESSING, version));
        Assertions.assertEquals(OrderStatus.PROCESSING, vo.getStatus());
        version = vo.getVersionNo();

        vo = orderService.transition(transition("PO-2005", OrderStatus.ACT_READY, version));
        Assertions.assertEquals(OrderStatus.READY, vo.getStatus());
        version = vo.getVersionNo();

        vo = orderService.transition(transition("PO-2005", OrderStatus.ACT_PICK, version));
        Assertions.assertEquals(OrderStatus.PICKED, vo.getStatus());

        // 已取镜不能再回退
        final long pickedVersion = vo.getVersionNo();
        Assertions.assertThrows(BizException.class, () -> orderService.transition(
                transition("PO-2005", OrderStatus.ACT_READY, pickedVersion)));
    }

    @Test
    void transition_staleVersion_conflict() {
        orderService.create(buildCreate("PO-2006", "-3.75", "-1.00", "-3.50", "-0.75"));

        orderService.transition(transition("PO-2006", OrderStatus.ACT_START_PROCESSING, 0L));
        BizException ex = Assertions.assertThrows(BizException.class,
                () -> orderService.transition(transition("PO-2006", OrderStatus.ACT_READY, 0L)));
        Assertions.assertEquals(BizCode.VERSION_CONFLICT.getCode(), ex.getBizCode().getCode());
    }

    @Test
    void cancel_thenRestore_prescriptionsFollow() {
        OrderVO vo = orderService.create(buildCreate("PO-2007", "-3.75", "-1.00", "-3.50", "-0.75"));

        OrderVO cancelled = orderService.transition(transition("PO-2007", OrderStatus.ACT_CANCEL, vo.getVersionNo()));
        Assertions.assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());

        // 冲销后按单号检索（主路径）不可见
        Assertions.assertThrows(BizException.class, () -> orderService.getByNo("PO-2007"));

        OrderVO restored = orderService.restore(transition("PO-2007", OrderStatus.ACT_RESTORE, cancelled.getVersionNo()));
        Assertions.assertEquals(OrderStatus.CREATED, restored.getStatus());
        Assertions.assertEquals(2, restored.getPrescriptions().size());
        // 处方随单经历冲销、恢复两次写入，版本号为 2
        restored.getPrescriptions().forEach(p -> Assertions.assertEquals(2L, p.getVersionNo()));
    }

    @Test
    void cancel_afterProcessing_rejected() {
        OrderVO vo = orderService.create(buildCreate("PO-2008", "-3.75", "-1.00", "-3.50", "-0.75"));
        orderService.transition(transition("PO-2008", OrderStatus.ACT_START_PROCESSING, vo.getVersionNo()));

        OrderVO processing = orderService.getByNo("PO-2008");
        BizException ex = Assertions.assertThrows(BizException.class,
                () -> orderService.transition(transition("PO-2008", OrderStatus.ACT_CANCEL, processing.getVersionNo())));
        Assertions.assertEquals(BizCode.ORDER_STATUS_INVALID.getCode(), ex.getBizCode().getCode());
    }

    @Test
    void getByNo_notFound() {
        BizException ex = Assertions.assertThrows(BizException.class,
                () -> orderService.getByNo("PO-NOT-EXIST"));
        Assertions.assertEquals(BizCode.ORDER_NOT_FOUND.getCode(), ex.getBizCode().getCode());
    }

    private OrderCreateRequest buildCreate(String orderNo,
                                           String lSphere, String lCylinder,
                                           String rSphere, String rCylinder) {
        OrderCreateRequest req = new OrderCreateRequest();
        req.setOrderNo(orderNo);
        req.setCustomerName("张三");
        req.setCustomerPhone("13800000000");
        req.setStoreCode("S001");
        req.setPrescriptions(Arrays.asList(
                prescription("L", lSphere, lCylinder, 90, "62.5"),
                prescription("R", rSphere, rCylinder, 85, "63.0")));
        return req;
    }

    private PrescriptionRequest prescription(String eye, String sphere, String cylinder,
                                             Integer axis, String pd) {
        PrescriptionRequest p = new PrescriptionRequest();
        p.setEye(eye);
        p.setSphere(new BigDecimal(sphere));
        p.setCylinder(new BigDecimal(cylinder));
        p.setAxis(axis);
        p.setPd(new BigDecimal(pd));
        return p;
    }

    private OrderTransitionRequest transition(String orderNo, String action, long versionNo) {
        OrderTransitionRequest t = new OrderTransitionRequest();
        t.setOrderNo(orderNo);
        t.setAction(action);
        t.setVersionNo(versionNo);
        return t;
    }
}
