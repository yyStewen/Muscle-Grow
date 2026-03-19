package com.musclegrow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.musclegrow.constant.VoucherStatusConstant;
import com.musclegrow.constant.VoucherStorageStatusConstant;
import com.musclegrow.context.BaseContext;
import com.musclegrow.entity.Voucher;
import com.musclegrow.entity.VoucherStorage;
import com.musclegrow.exception.BaseException;
import com.musclegrow.mapper.VoucherMapper;
import com.musclegrow.mapper.VoucherStorageMapper;
import com.musclegrow.service.UserVoucherService;
import com.musclegrow.vo.UserVoucherVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserVoucherServiceImpl implements UserVoucherService {

    private static final String MSG_USER_NOT_LOGIN = "\u7528\u6237\u672a\u767b\u5f55";
    private static final String MSG_VOUCHER_NOT_FOUND = "\u4f18\u60e0\u5238\u4e0d\u5b58\u5728";
    private static final String MSG_VOUCHER_NOT_STARTED = "\u8be5\u4f18\u60e0\u5238\u5c1a\u672a\u5f00\u59cb\u6295\u653e";
    private static final String MSG_VOUCHER_OFFLINE = "\u8be5\u4f18\u60e0\u5238\u4e0d\u5728\u53ef\u8d2d\u4e70\u72b6\u6001";
    private static final String MSG_VOUCHER_ENDED = "\u8be5\u4f18\u60e0\u5238\u5df2\u7ed3\u675f";
    private static final String MSG_VOUCHER_SOLD_OUT = "\u8be5\u4f18\u60e0\u5238\u5df2\u552e\u7f44";
    private static final String MSG_VOUCHER_ALREADY_PURCHASED = "\u8be5\u4f18\u60e0\u5238\u6bcf\u4eba\u53ea\u80fd\u8d2d\u4e70\u4e00\u6b21";

    @Autowired
    private VoucherMapper voucherMapper;

    @Autowired
    private VoucherStorageMapper voucherStorageMapper;

    @Override
    public List<UserVoucherVO> listAvailable() {
        // 用户端只展示当前还能购买的券，因此先把自然过期的数据同步掉。
        syncExpiredStatus();

        Long userId = getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();

        List<Voucher> vouchers = voucherMapper.selectList(new LambdaQueryWrapper<Voucher>()
                .eq(Voucher::getStatus, VoucherStatusConstant.ON_SALE)
                .le(Voucher::getBeginTime, now)
                .ge(Voucher::getEndTime, now)
                .orderByAsc(Voucher::getEndTime)
                .orderByDesc(Voucher::getActualValue));

        // 单独查用户已拥有的券，用于前端直接标记“已购买”。
        Set<Long> purchasedVoucherIds = getPurchasedVoucherIds(userId, vouchers);

        return vouchers.stream()
                .map(voucher -> UserVoucherVO.builder()
                        .id(voucher.getId())
                        .title(voucher.getTitle())
                        .payValue(voucher.getPayValue())
                        .actualValue(voucher.getActualValue())
                        .stock(voucher.getStock())
                        .beginTime(voucher.getBeginTime())
                        .endTime(voucher.getEndTime())
                        .purchased(purchasedVoucherIds.contains(voucher.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Long purchase(Long voucherId) {
        Long userId = getCurrentUserId();
        // 同一 JVM 内对“同一用户购买同一张券”做串行化，降低重复下单窗口。
        String lockKey = ("voucher:purchase:" + userId + ":" + voucherId).intern();

        synchronized (lockKey) {
            return doPurchase(userId, voucherId);
        }
    }

    private Long doPurchase(Long userId, Long voucherId) {
        // 购买前再次同步状态，避免刚过期的券继续被领取。
        syncExpiredStatus();

        LocalDateTime now = LocalDateTime.now();
        Voucher voucher = voucherMapper.selectById(voucherId);
        if (voucher == null) {
            throw new BaseException(MSG_VOUCHER_NOT_FOUND);
        }

        if (!Objects.equals(voucher.getStatus(), VoucherStatusConstant.ON_SALE)) {
            throw new BaseException(MSG_VOUCHER_OFFLINE);
        }

        if (voucher.getBeginTime() != null && voucher.getBeginTime().isAfter(now)) {
            throw new BaseException(MSG_VOUCHER_NOT_STARTED);
        }

        if (voucher.getEndTime() != null && voucher.getEndTime().isBefore(now)) {
            throw new BaseException(MSG_VOUCHER_ENDED);
        }

        if (voucher.getStock() == null || voucher.getStock() <= 0) {
            throw new BaseException(MSG_VOUCHER_SOLD_OUT);
        }

        boolean alreadyPurchased = hasPurchased(userId, voucherId);
        if (alreadyPurchased) {
            throw new BaseException(MSG_VOUCHER_ALREADY_PURCHASED);
        }

        // 库存扣减放到数据库原子更新里处理，避免并发下出现超卖。
        int affected = voucherMapper.deductStock(voucherId, now);
        if (affected == 0) {
            throw new BaseException(MSG_VOUCHER_SOLD_OUT);
        }

        // 领券时冗余保存标题、面额和过期时间，后续即使券主表被改动，用户券快照仍然稳定。
        VoucherStorage voucherStorage = VoucherStorage.builder()
                .userId(userId)
                .voucherId(voucherId)
                .orderId(null)
                .name(voucher.getTitle())
                .actualValue(voucher.getActualValue())
                .status(VoucherStorageStatusConstant.UNUSED)
                .createTime(now)
                .expireTime(voucher.getEndTime())
                .build();
        voucherStorageMapper.insert(voucherStorage);

        return voucherStorage.getId();
    }

    private Set<Long> getPurchasedVoucherIds(Long userId, List<Voucher> vouchers) {
        if (vouchers == null || vouchers.isEmpty()) {
            return Collections.emptySet();
        }

        List<Long> voucherIds = vouchers.stream().map(Voucher::getId).collect(Collectors.toList());
        List<VoucherStorage> voucherStorageList = voucherStorageMapper.selectList(new LambdaQueryWrapper<VoucherStorage>()
                .eq(VoucherStorage::getUserId, userId)
                .in(VoucherStorage::getVoucherId, voucherIds));

        return voucherStorageList.stream()
                .map(VoucherStorage::getVoucherId)
                .collect(Collectors.toSet());
    }

    private boolean hasPurchased(Long userId, Long voucherId) {
        Long count = voucherStorageMapper.selectCount(new LambdaQueryWrapper<VoucherStorage>()
                .eq(VoucherStorage::getUserId, userId)
                .eq(VoucherStorage::getVoucherId, voucherId));
        return count != null && count > 0;
    }

    private Long getCurrentUserId() {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException(MSG_USER_NOT_LOGIN);
        }
        return userId;
    }

    private void syncExpiredStatus() {
        Voucher voucher = Voucher.builder()
                .status(VoucherStatusConstant.ENDED)
                .build();

        LambdaUpdateWrapper<Voucher> updateWrapper = new LambdaUpdateWrapper<>();
        // 用户端和管理端共享同一套过期同步逻辑，保证两边看到的状态一致。
        updateWrapper.ne(Voucher::getStatus, VoucherStatusConstant.ENDED)
                .lt(Voucher::getEndTime, LocalDateTime.now());

        voucherMapper.update(voucher, updateWrapper);
    }
}
