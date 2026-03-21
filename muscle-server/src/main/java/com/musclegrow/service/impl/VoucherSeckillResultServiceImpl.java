package com.musclegrow.service.impl;

import com.alibaba.fastjson.JSON;
import com.musclegrow.constant.VoucherSeckillRedisKeyConstant;
import com.musclegrow.constant.VoucherSeckillResultStatusConstant;
import com.musclegrow.service.VoucherSeckillResultService;
import com.musclegrow.vo.VoucherSeckillResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class VoucherSeckillResultServiceImpl implements VoucherSeckillResultService {

    private static final Duration RESULT_TTL = Duration.ofMinutes(30);
    private static final String MSG_PROCESSING = "\u79d2\u6740\u8bf7\u6c42\u5904\u7406\u4e2d";
    private static final String MSG_RESULT_MISSING = "\u79d2\u6740\u7ed3\u679c\u4e0d\u5b58\u5728\u6216\u5df2\u8fc7\u671f";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void saveProcessing(String requestId) {
        VoucherSeckillResultVO result = VoucherSeckillResultVO.builder()
                .requestId(requestId)
                .status(VoucherSeckillResultStatusConstant.PROCESSING)
                .message(MSG_PROCESSING)
                .build();
        saveResult(result);
    }

    @Override
    public void saveSuccess(String requestId, Long orderId, String orderNumber) {
        VoucherSeckillResultVO result = VoucherSeckillResultVO.builder()
                .requestId(requestId)
                .status(VoucherSeckillResultStatusConstant.SUCCESS)
                .orderId(orderId)
                .orderNumber(orderNumber)
                .build();
        saveResult(result);
    }

    @Override
    public void saveFailed(String requestId, String message) {
        VoucherSeckillResultVO result = VoucherSeckillResultVO.builder()
                .requestId(requestId)
                .status(VoucherSeckillResultStatusConstant.FAILED)
                .message(message)
                .build();
        saveResult(result);
    }

    @Override
    public VoucherSeckillResultVO getResult(String requestId) {
        String json = stringRedisTemplate.opsForValue().get(resultKey(requestId));
        if (json == null || json.isEmpty()) {
            return VoucherSeckillResultVO.builder()
                    .requestId(requestId)
                    .status(VoucherSeckillResultStatusConstant.FAILED)
                    .message(MSG_RESULT_MISSING)
                    .build();
        }
        return JSON.parseObject(json, VoucherSeckillResultVO.class);
    }

    private void saveResult(VoucherSeckillResultVO result) {
        stringRedisTemplate.opsForValue().set(
                resultKey(result.getRequestId()),
                JSON.toJSONString(result),
                RESULT_TTL
        );
    }

    private String resultKey(String requestId) {
        return VoucherSeckillRedisKeyConstant.VOUCHER_RESULT + requestId;
    }
}
