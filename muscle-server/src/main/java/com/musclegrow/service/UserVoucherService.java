package com.musclegrow.service;

import com.musclegrow.vo.OrderSubmitVO;
import com.musclegrow.vo.VoucherSeckillResultVO;
import com.musclegrow.vo.VoucherSeckillSubmitVO;
import com.musclegrow.vo.UserVoucherVO;

import java.util.List;

public interface UserVoucherService {

    List<UserVoucherVO> listAvailable();

    VoucherSeckillSubmitVO seckill(Long voucherId);

    VoucherSeckillResultVO getSeckillResult(String requestId);

    OrderSubmitVO purchase(Long voucherId);
}
