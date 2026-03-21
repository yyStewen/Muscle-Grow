package com.musclegrow.service.impl;

import com.musclegrow.constant.VoucherSeckillRedisKeyConstant;
import com.musclegrow.service.VoucherOrderNumberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Service
public class VoucherOrderNumberServiceImpl implements VoucherOrderNumberService {

    private static final DateTimeFormatter DATE_KEY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter ORDER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public String nextOrderNumber() {
        LocalDateTime now = LocalDateTime.now();
        String datePart = now.format(DATE_KEY_FORMATTER);
        String key = VoucherSeckillRedisKeyConstant.VOUCHER_ORDER_NO + datePart;
        Long sequence = stringRedisTemplate.opsForValue().increment(key);
        stringRedisTemplate.expire(key, 2, TimeUnit.DAYS);
        long currentSequence = sequence == null ? 0L : sequence;
        return "VO" + now.format(ORDER_NO_FORMATTER) + String.format("%06d", currentSequence);
    }
}
