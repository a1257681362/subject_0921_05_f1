package com.evops.lens;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evops.common.ApiResponse;
import com.evops.common.PageResult;
import com.evops.lens.dto.LensCreateRequest;
import com.evops.lens.dto.LensRevokeRequest;
import com.evops.lens.dto.LensUpdateRequest;
import com.evops.lens.dto.LensVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/lens")
public class LensController {

    private final LensService lensService;

    public LensController(LensService lensService) {
        this.lensService = lensService;
    }

    @PostMapping("/create")
    public ApiResponse<LensVO> create(@Valid @RequestBody LensCreateRequest request) {
        return ApiResponse.ok(lensService.create(request));
    }

    @PostMapping("/update")
    public ApiResponse<LensVO> update(@Valid @RequestBody LensUpdateRequest request) {
        return ApiResponse.ok(lensService.update(request));
    }

    @PostMapping("/revoke")
    public ApiResponse<LensVO> revoke(@Valid @RequestBody LensRevokeRequest request) {
        return ApiResponse.ok(lensService.revoke(request));
    }

    @PostMapping("/restore")
    public ApiResponse<LensVO> restore(@Valid @RequestBody LensRevokeRequest request) {
        return ApiResponse.ok(lensService.restore(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<LensVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(lensService.getById(id));
    }

    @GetMapping("/page")
    public ApiResponse<PageResult<LensVO>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        Page<LensVO> page = lensService.page(current, size, keyword);
        return ApiResponse.ok(new PageResult<>(page.getCurrent(), page.getSize(),
                page.getTotal(), page.getRecords()));
    }
}
