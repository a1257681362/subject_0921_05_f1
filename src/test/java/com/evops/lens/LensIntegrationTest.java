package com.evops.lens;

import com.evops.common.BizCode;
import com.evops.common.BizException;
import com.evops.common.OperatorContext;
import com.evops.lens.dto.LensCreateRequest;
import com.evops.lens.dto.LensRevokeRequest;
import com.evops.lens.dto.LensUpdateRequest;
import com.evops.lens.dto.LensVO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:lens-test;DB_CLOSE_DELAY=-1",
        "spring.sql.init.mode=always"
})
class LensIntegrationTest {

    @Autowired
    private LensService lensService;

    @BeforeEach
    void setUp() {
        OperatorContext.set("tester01", "REQ-LENS-0001", LocalDateTime.of(2026, 9, 26, 10, 0));
    }

    @Test
    void create_shouldPersistAuditFields() {
        LensCreateRequest req = new LensCreateRequest();
        req.setLensCode("L-1001");
        req.setLensName("1.60非球面");
        req.setBrand("明月");
        req.setLensType("单光");
        req.setUnitPrice(new BigDecimal("380.00"));

        LensVO vo = lensService.create(req);

        Assertions.assertEquals(0L, vo.getVersionNo());
        Assertions.assertEquals("ACTIVE", vo.getStatus());
        Assertions.assertEquals("tester01", vo.getOperator());
        Assertions.assertEquals("REQ-LENS-0001", vo.getRequestNo());
        Assertions.assertEquals(LocalDateTime.of(2026, 9, 26, 10, 0), vo.getBizTime());
    }

    @Test
    void create_duplicateCode_returnsLensDuplicate() {
        createLens("L-1002", "镜片A");

        LensCreateRequest again = new LensCreateRequest();
        again.setLensCode("L-1002");
        again.setLensName("镜片A-重复");
        again.setUnitPrice(new BigDecimal("100.00"));

        BizException ex = Assertions.assertThrows(BizException.class, () -> lensService.create(again));
        Assertions.assertEquals(BizCode.LENS_DUPLICATE.getCode(), ex.getBizCode().getCode());
    }

    @Test
    void update_withStaleVersion_conflict() {
        LensVO vo = createLens("L-1003", "镜片C");

        LensUpdateRequest first = new LensUpdateRequest();
        first.setId(vo.getId());
        first.setVersionNo(0L);
        first.setLensName("镜片C-改1");
        first.setUnitPrice(new BigDecimal("200.00"));
        lensService.update(first);

        LensUpdateRequest stale = new LensUpdateRequest();
        stale.setId(vo.getId());
        stale.setVersionNo(0L);
        stale.setLensName("镜片C-改2");
        BizException ex = Assertions.assertThrows(BizException.class, () -> lensService.update(stale));
        Assertions.assertEquals(BizCode.VERSION_CONFLICT.getCode(), ex.getBizCode().getCode());
    }

    @Test
    void revoke_thenRestore_fullCycle() {
        LensVO vo = createLens("L-1004", "镜片D");

        LensRevokeRequest revoke = new LensRevokeRequest();
        revoke.setId(vo.getId());
        revoke.setVersionNo(0L);
        revoke.setReason("停售");
        LensVO revoked = lensService.revoke(revoke);
        Assertions.assertEquals("REVOKED", revoked.getStatus());
        Assertions.assertEquals(1L, revoked.getVersionNo());

        // 冲销后按正常查询不可见
        Assertions.assertThrows(BizException.class, () -> lensService.getById(vo.getId()));
        // 不能重复冲销
        LensRevokeRequest duplicateRevoke = new LensRevokeRequest();
        duplicateRevoke.setId(vo.getId());
        duplicateRevoke.setVersionNo(1L);
        Assertions.assertThrows(BizException.class, () -> lensService.revoke(duplicateRevoke));

        LensRevokeRequest restore = new LensRevokeRequest();
        restore.setId(vo.getId());
        restore.setVersionNo(1L);
        LensVO active = lensService.restore(restore);
        Assertions.assertEquals("ACTIVE", active.getStatus());
        Assertions.assertEquals(2L, active.getVersionNo());
    }

    private LensVO createLens(String code, String name) {
        LensCreateRequest req = new LensCreateRequest();
        req.setLensCode(code);
        req.setLensName(name);
        req.setUnitPrice(new BigDecimal("100.00"));
        return lensService.create(req);
    }
}
