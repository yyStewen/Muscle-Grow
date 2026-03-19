package com.musclegrow.controller.admin;

import com.musclegrow.dto.VoucherDTO;
import com.musclegrow.dto.VoucherPageQueryDTO;
import com.musclegrow.result.PageResult;
import com.musclegrow.result.Result;
import com.musclegrow.service.VoucherService;
import com.musclegrow.vo.VoucherVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/voucher")
@Api(tags = "Voucher Admin API")
@Slf4j
public class VoucherController {

    @Autowired
    private VoucherService voucherService;

    @GetMapping("/page")
    @ApiOperation("Page query vouchers")
    public Result<PageResult> page(VoucherPageQueryDTO voucherPageQueryDTO) {
        log.info("voucher page query: {}", voucherPageQueryDTO);
        return Result.success(voucherService.pageQuery(voucherPageQueryDTO));
    }

    @PostMapping
    @ApiOperation("Create voucher")
    public Result<String> save(@RequestBody VoucherDTO voucherDTO) {
        log.info("create voucher: {}", voucherDTO);
        voucherService.save(voucherDTO);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("Get voucher by id")
    public Result<VoucherVO> getById(@PathVariable Long id) {
        return Result.success(voucherService.getById(id));
    }

    @PutMapping
    @ApiOperation("Update voucher")
    public Result<String> update(@RequestBody VoucherDTO voucherDTO) {
        log.info("update voucher: {}", voucherDTO);
        voucherService.update(voucherDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("Update voucher status")
    public Result<String> startOrStop(@PathVariable Integer status, Long id) {
        log.info("update voucher status, status={}, id={}", status, id);
        voucherService.startOrStop(status, id);
        return Result.success();
    }
}
