package com.musclegrow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.musclegrow.constant.VoucherOrderConstant;
import com.musclegrow.entity.OrderDetail;
import com.musclegrow.entity.Orders;
import com.musclegrow.entity.User;
import com.musclegrow.entity.Voucher;
import com.musclegrow.exception.BaseException;
import com.musclegrow.mapper.OrderDetailMapper;
import com.musclegrow.mapper.OrderMapper;
import com.musclegrow.mapper.UserMapper;
import com.musclegrow.mapper.VoucherMapper;
import com.musclegrow.message.VoucherOrderCreateMessage;
import com.musclegrow.service.VoucherOrderAsyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class VoucherOrderAsyncServiceImpl implements VoucherOrderAsyncService {

    private static final String MSG_VOUCHER_NOT_FOUND = "\u4f18\u60e0\u5238\u4e0d\u5b58\u5728";
    private static final String MSG_VOUCHER_NOT_AVAILABLE = "\u8be5\u4f18\u60e0\u5238\u5df2\u4e0b\u67b6\u3001\u8fc7\u671f\u6216\u5df2\u552e\u7f44";

    @Autowired
    private VoucherMapper voucherMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public Orders createPendingVoucherOrder(VoucherOrderCreateMessage message) {
        Orders existingOrder = orderMapper.selectOne(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getNumber, message.getOrderNumber())
                .last("limit 1"));
        if (existingOrder != null) {
            return existingOrder;
        }

        Voucher voucher = voucherMapper.selectById(message.getVoucherId());
        if (voucher == null) {
            throw new BaseException(MSG_VOUCHER_NOT_FOUND);
        }

        LocalDateTime now = LocalDateTime.now();
        int affected = voucherMapper.deductStock(message.getVoucherId(), now);
        if (affected == 0) {
            throw new BaseException(MSG_VOUCHER_NOT_AVAILABLE);
        }

        User user = userMapper.selectById(message.getUserId());
        Orders order = Orders.builder()
                .number(message.getOrderNumber())
                .status(Orders.PENDING_PAYMENT)
                .userId(message.getUserId())
                .addressBookId(0L)
                .orderTime(message.getRequestTime() == null ? now : message.getRequestTime())
                .payMethod(1)
                .payStatus(Orders.UN_PAID)
                .amount(message.getPayAmount())
                .remark(VoucherOrderConstant.SECKILL_VOUCHER_ORDER_REMARK)
                .userName(user != null ? user.getName() : null)
                .mailAmount(0)
                .build();
        orderMapper.insert(order);

        OrderDetail orderDetail = OrderDetail.builder()
                .orderId(order.getId())
                .voucherId(message.getVoucherId())
                .name(voucher.getTitle())
                .number(1)
                .amount(message.getPayAmount())
                .build();
        orderDetailMapper.insert(orderDetail);
        return order;
    }
}
