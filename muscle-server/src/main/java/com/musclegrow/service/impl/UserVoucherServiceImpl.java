package com.musclegrow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.musclegrow.constant.VoucherOrderConstant;
import com.musclegrow.constant.VoucherSeckillResultStatusConstant;
import com.musclegrow.constant.VoucherStatusConstant;
import com.musclegrow.context.BaseContext;
import com.musclegrow.entity.OrderDetail;
import com.musclegrow.entity.Orders;
import com.musclegrow.entity.User;
import com.musclegrow.entity.Voucher;
import com.musclegrow.entity.VoucherStorage;
import com.musclegrow.exception.BaseException;
import com.musclegrow.message.VoucherOrderCreateMessage;
import com.musclegrow.mapper.OrderDetailMapper;
import com.musclegrow.mapper.OrderMapper;
import com.musclegrow.mapper.UserMapper;
import com.musclegrow.mapper.VoucherMapper;
import com.musclegrow.mapper.VoucherStorageMapper;
import com.musclegrow.service.UserVoucherService;
import com.musclegrow.service.VoucherOrderNumberService;
import com.musclegrow.service.VoucherOrderProducer;
import com.musclegrow.service.VoucherSeckillRedisService;
import com.musclegrow.service.VoucherSeckillResultService;
import com.musclegrow.vo.OrderSubmitVO;
import com.musclegrow.vo.VoucherSeckillResultVO;
import com.musclegrow.vo.VoucherSeckillSubmitVO;
import com.musclegrow.vo.UserVoucherVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
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
    private static final String MSG_VOUCHER_PENDING_EXISTS = "\u8be5\u4f18\u60e0\u5238\u5b58\u5728\u5f85\u652f\u4ed8\u8ba2\u5355\uff0c\u8bf7\u5148\u5b8c\u6210\u652f\u4ed8";
    private static final String MSG_SECKILL_PROCESSING = "\u79d2\u6740\u8bf7\u6c42\u5904\u7406\u4e2d";
    private static final String MSG_SECKILL_SUBMIT_FAILED = "\u79d2\u6740\u4e0b\u5355\u63d0\u4ea4\u5931\u8d25";

    @Autowired
    private VoucherMapper voucherMapper;

    @Autowired
    private VoucherStorageMapper voucherStorageMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private VoucherSeckillRedisService voucherSeckillRedisService;

    @Autowired
    private VoucherSeckillResultService voucherSeckillResultService;

    @Autowired
    private VoucherOrderProducer voucherOrderProducer;

    @Autowired
    private VoucherOrderNumberService voucherOrderNumberService;

    @Override
    public List<UserVoucherVO> listAvailable() {
        syncExpiredStatus();

        Long userId = getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();

        List<Voucher> vouchers = voucherMapper.selectList(new LambdaQueryWrapper<Voucher>()
                .eq(Voucher::getStatus, VoucherStatusConstant.ON_SALE)
                .le(Voucher::getBeginTime, now)
                .ge(Voucher::getEndTime, now)
                .orderByAsc(Voucher::getEndTime)
                .orderByDesc(Voucher::getActualValue));

        Set<Long> purchasedVoucherIds = getPurchasedVoucherIds(userId, vouchers);
        Map<Long, Long> pendingVoucherOrderMap = getPendingVoucherOrderMap(userId, vouchers);

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
                        .pendingOrderId(pendingVoucherOrderMap.get(voucher.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public VoucherSeckillSubmitVO seckill(Long voucherId) {
        Long userId = getCurrentUserId();
        Voucher voucher = loadSeckillVoucherOrThrow(voucherId);

        if (hasPurchased(userId, voucherId)) {
            voucherSeckillRedisService.confirmPurchase(voucherId, userId);
            throw new BaseException(MSG_VOUCHER_ALREADY_PURCHASED);
        }

        Long pendingOrderId = getPendingVoucherOrderId(userId, voucherId);
        if (pendingOrderId != null) {
            throw new BaseException(MSG_VOUCHER_PENDING_EXISTS);
        }

        voucherSeckillRedisService.preloadStock(voucherId, voucher.getStock());
        Long reserveResult = voucherSeckillRedisService.tryReserve(voucherId, userId);
        if (reserveResult == null) {
            throw new BaseException(MSG_SECKILL_SUBMIT_FAILED);
        }
        if (reserveResult == 1L) {
            throw new BaseException(MSG_VOUCHER_SOLD_OUT);
        }
        if (reserveResult == 2L) {
            throw new BaseException(MSG_VOUCHER_ALREADY_PURCHASED);
        }
        if (reserveResult == 3L) {
            throw new BaseException(MSG_VOUCHER_PENDING_EXISTS);
        }

        String requestId = UUID.randomUUID().toString().replace("-", "");
        VoucherOrderCreateMessage message = VoucherOrderCreateMessage.builder()
                .requestId(requestId)
                .userId(userId)
                .voucherId(voucherId)
                .orderNumber(voucherOrderNumberService.nextOrderNumber())
                .payAmount(voucher.getPayValue())
                .requestTime(LocalDateTime.now())
                .build();

        voucherSeckillResultService.saveProcessing(requestId);
        try {
            voucherOrderProducer.sendCreateOrderMessage(message);
        } catch (Exception ex) {
            voucherSeckillRedisService.rollbackReservation(voucherId, userId);
            voucherSeckillResultService.saveFailed(requestId, MSG_SECKILL_SUBMIT_FAILED);
            throw new BaseException(MSG_SECKILL_SUBMIT_FAILED);
        }

        return VoucherSeckillSubmitVO.builder()
                .requestId(requestId)
                .status(VoucherSeckillResultStatusConstant.PROCESSING)
                .message(MSG_SECKILL_PROCESSING)
                .build();
    }

    @Override
    public VoucherSeckillResultVO getSeckillResult(String requestId) {
        return voucherSeckillResultService.getResult(requestId);
    }

    @Override
    @Transactional
    public OrderSubmitVO purchase(Long voucherId) {
        Long userId = getCurrentUserId();
        String lockKey = ("voucher:purchase:" + userId + ":" + voucherId).intern();

        synchronized (lockKey) {
            return doPurchase(userId, voucherId);
        }
    }

    private OrderSubmitVO doPurchase(Long userId, Long voucherId) {
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

        if (hasPurchased(userId, voucherId) || hasPendingVoucherOrder(userId, voucherId)) {
            throw new BaseException(MSG_VOUCHER_ALREADY_PURCHASED);
        }

        User user = userMapper.selectById(userId);
        Orders order = Orders.builder()
                .number(String.valueOf(System.currentTimeMillis()))
                .status(Orders.PENDING_PAYMENT)
                .userId(userId)
                .addressBookId(0L)
                .orderTime(now)
                .payMethod(1)
                .payStatus(Orders.UN_PAID)
                .amount(voucher.getPayValue())
                .remark(VoucherOrderConstant.LEGACY_VOUCHER_ORDER_REMARK)
                .userName(user != null ? user.getName() : null)
                .mailAmount(0)
                .build();
        orderMapper.insert(order);

        OrderDetail orderDetail = OrderDetail.builder()
                .orderId(order.getId())
                .voucherId(voucherId)
                .name(voucher.getTitle())
                .number(1)
                .amount(voucher.getPayValue())
                .build();
        orderDetailMapper.insert(orderDetail);

        return OrderSubmitVO.builder()
                .id(order.getId())
                .orderNumber(order.getNumber())
                .orderAmount(order.getAmount())
                .orderTime(order.getOrderTime())
                .build();
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

    private Map<Long, Long> getPendingVoucherOrderMap(Long userId, List<Voucher> vouchers) {
        if (vouchers == null || vouchers.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> pendingOrderIds = listPendingVoucherOrderIds(userId);
        if (pendingOrderIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> voucherIds = vouchers.stream().map(Voucher::getId).collect(Collectors.toList());
        List<OrderDetail> orderDetails = orderDetailMapper.selectList(new LambdaQueryWrapper<OrderDetail>()
                .select(OrderDetail::getOrderId, OrderDetail::getVoucherId)
                .in(OrderDetail::getOrderId, pendingOrderIds)
                .in(OrderDetail::getVoucherId, voucherIds));

        return orderDetails.stream()
                .filter(detail -> detail.getVoucherId() != null)
                .collect(Collectors.toMap(
                        OrderDetail::getVoucherId,
                        OrderDetail::getOrderId,
                        (left, right) -> left
                ));
    }

    private boolean hasPurchased(Long userId, Long voucherId) {
        Long count = voucherStorageMapper.selectCount(new LambdaQueryWrapper<VoucherStorage>()
                .eq(VoucherStorage::getUserId, userId)
                .eq(VoucherStorage::getVoucherId, voucherId));
        return count != null && count > 0;
    }

    private Long getPendingVoucherOrderId(Long userId, Long voucherId) {
        List<Long> pendingOrderIds = listPendingVoucherOrderIds(userId);
        if (pendingOrderIds.isEmpty()) {
            return null;
        }

        OrderDetail orderDetail = orderDetailMapper.selectOne(new LambdaQueryWrapper<OrderDetail>()
                .select(OrderDetail::getOrderId)
                .in(OrderDetail::getOrderId, pendingOrderIds)
                .eq(OrderDetail::getVoucherId, voucherId)
                .last("limit 1"));
        return orderDetail == null ? null : orderDetail.getOrderId();
    }

    private boolean hasPendingVoucherOrder(Long userId, Long voucherId) {
        return getPendingVoucherOrderId(userId, voucherId) != null;
    }

    private List<Long> listPendingVoucherOrderIds(Long userId) {
        List<Orders> orders = orderMapper.selectList(new LambdaQueryWrapper<Orders>()
                .select(Orders::getId)
                .eq(Orders::getUserId, userId)
                .eq(Orders::getStatus, Orders.PENDING_PAYMENT));

        if (orders == null || orders.isEmpty()) {
            return Collections.emptyList();
        }

        return orders.stream().map(Orders::getId).collect(Collectors.toList());
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
        updateWrapper.ne(Voucher::getStatus, VoucherStatusConstant.ENDED)
                .lt(Voucher::getEndTime, LocalDateTime.now());

        voucherMapper.update(voucher, updateWrapper);
    }

    private Voucher loadSeckillVoucherOrThrow(Long voucherId) {
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
        return voucher;
    }
}
