package com.musclegrow.controller.user;

import com.musclegrow.result.Result;
import com.musclegrow.service.UserVoucherService;
import com.musclegrow.vo.OrderSubmitVO;
import com.musclegrow.vo.VoucherSeckillResultVO;
import com.musclegrow.vo.VoucherSeckillSubmitVO;
import com.musclegrow.vo.UserVoucherVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userVoucherController")
@RequestMapping("/user/voucher")
@Api(tags = "User voucher API")
@Slf4j
public class VoucherController {

    @Autowired
    private UserVoucherService userVoucherService;

    @GetMapping("/list")
    @ApiOperation("List available vouchers for user")
    public Result<List<UserVoucherVO>> list() {
        return Result.success(userVoucherService.listAvailable());
    }

    @PostMapping("/seckill/{id}")
    @ApiOperation("Voucher seckill")
    public Result<VoucherSeckillSubmitVO> seckill(@PathVariable("id") Long id) {
        log.info("user seckill voucher, voucherId={}", id);
        return Result.success(userVoucherService.seckill(id));
    }

    @GetMapping("/seckill/result/{requestId}")
    @ApiOperation("Query voucher seckill result")
    public Result<VoucherSeckillResultVO> getSeckillResult(@PathVariable String requestId) {
        return Result.success(userVoucherService.getSeckillResult(requestId));
    }

    @PostMapping("/purchase/{id}")
    @ApiOperation("Purchase voucher")
    public Result<OrderSubmitVO> purchase(@PathVariable("id") Long id) {
        log.info("user purchase voucher, voucherId={}", id);
        return Result.success(userVoucherService.purchase(id));
    }
}
