package com.musclegrow.service.impl;

import com.musclegrow.constant.VoucherSeckillRedisKeyConstant;
import com.musclegrow.service.VoucherSeckillRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Service
public class VoucherSeckillRedisServiceImpl implements VoucherSeckillRedisService {

    private static final Duration PENDING_TTL = Duration.ofMinutes(20);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private DefaultRedisScript<Long> voucherSeckillRedisScript;

    @Override
    public Long tryReserve(Long voucherId, Long userId) {
        return stringRedisTemplate.execute(
                voucherSeckillRedisScript,
                Arrays.asList(
                        stockKey(voucherId),
                        boughtKey(voucherId),
                        pendingKey(voucherId, userId)
                ),
                String.valueOf(userId),
                String.valueOf(PENDING_TTL.getSeconds())
        );
    }

    @Override
    public void preloadStock(Long voucherId, Integer stock) {
        if (voucherId == null || stock == null || stock < 0) {
            return;
        }
        stringRedisTemplate.opsForValue().setIfAbsent(stockKey(voucherId), String.valueOf(stock));
    }

    @Override
    public void refreshStock(Long voucherId, Integer stock) {
        if (voucherId == null || stock == null || stock < 0) {
            return;
        }
        stringRedisTemplate.opsForValue().set(stockKey(voucherId), String.valueOf(stock));
    }

    @Override
    public void removeStock(Long voucherId) {
        if (voucherId == null) {
            return;
        }
        stringRedisTemplate.delete(stockKey(voucherId));
    }

    @Override
    public void rollbackReservation(Long voucherId, Long userId) {
        if (voucherId == null || userId == null) {
            return;
        }
        String pendingKey = pendingKey(voucherId, userId);
        Boolean existed = stringRedisTemplate.hasKey(pendingKey);
        if (Boolean.TRUE.equals(existed)) {
            stringRedisTemplate.opsForValue().increment(stockKey(voucherId));
            stringRedisTemplate.delete(pendingKey);
        }
    }

    @Override
    public void confirmPurchase(Long voucherId, Long userId) {
        if (voucherId == null || userId == null) {
            return;
        }
        stringRedisTemplate.opsForSet().add(boughtKey(voucherId), String.valueOf(userId));
        stringRedisTemplate.delete(pendingKey(voucherId, userId));
    }

    @Override
    public boolean hasBought(Long voucherId, Long userId) {
        if (voucherId == null || userId == null) {
            return false;
        }
        Boolean member = stringRedisTemplate.opsForSet().isMember(boughtKey(voucherId), String.valueOf(userId));
        return Boolean.TRUE.equals(member);
    }

    private String stockKey(Long voucherId) {
        return VoucherSeckillRedisKeyConstant.VOUCHER_STOCK + voucherId;
    }

    private String boughtKey(Long voucherId) {
        return VoucherSeckillRedisKeyConstant.VOUCHER_BOUGHT + voucherId;
    }

    private String pendingKey(Long voucherId, Long userId) {
        return VoucherSeckillRedisKeyConstant.VOUCHER_PENDING + voucherId + ":" + userId;
    }
}
