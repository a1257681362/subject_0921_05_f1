package com.evops.order;

import com.evops.common.ApiResponse;
import com.evops.order.dto.OrderCreateRequest;
import com.evops.order.dto.OrderTransitionRequest;
import com.evops.order.dto.OrderVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/orders")
public class ProcessOrderController {

    private final ProcessOrderService orderService;

    public ProcessOrderController(ProcessOrderService orderService) {
        this.orderService = orderService;
    }

    /** 加工单建档（含左右眼处方，同事务生效）。 */
    @PostMapping("/create")
    public ApiResponse<OrderVO> create(@Valid @RequestBody OrderCreateRequest request) {
        return ApiResponse.ok(orderService.create(request));
    }

    /** 状态流转：START_PROCESSING / READY / PICK / CANCEL。 */
    @PostMapping("/transition")
    public ApiResponse<OrderVO> transition(@Valid @RequestBody OrderTransitionRequest request) {
        return ApiResponse.ok(orderService.transition(request));
    }

    /** 已冲销单据恢复（action 传 RESTORE）。 */
    @PostMapping("/restore")
    public ApiResponse<OrderVO> restore(@Valid @RequestBody OrderTransitionRequest request) {
        request.setAction(OrderStatus.ACT_RESTORE);
        return ApiResponse.ok(orderService.restore(request));
    }

    /** 按加工单号检索。 */
    @GetMapping("/{orderNo}")
    public ApiResponse<OrderVO> detail(@PathVariable String orderNo) {
        return ApiResponse.ok(orderService.getByNo(orderNo));
    }
}
