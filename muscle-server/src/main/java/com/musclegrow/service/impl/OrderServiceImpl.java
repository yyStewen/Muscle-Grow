package com.musclegrow.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.musclegrow.constant.MessageConstant;
import com.musclegrow.constant.VoucherStorageStatusConstant;
import com.musclegrow.context.BaseContext;
import com.musclegrow.dto.OrdersCancelDTO;
import com.musclegrow.dto.OrdersConfirmDTO;
import com.musclegrow.dto.OrdersPageQueryDTO;
import com.musclegrow.dto.OrdersPaymentDTO;
import com.musclegrow.dto.OrdersRejectionDTO;
import com.musclegrow.dto.OrdersSubmitDTO;
import com.musclegrow.entity.AddressBook;
import com.musclegrow.entity.OrderDetail;
import com.musclegrow.entity.Orders;
import com.musclegrow.entity.ShoppingCart;
import com.musclegrow.entity.User;
import com.musclegrow.entity.Voucher;
import com.musclegrow.entity.VoucherStorage;
import com.musclegrow.exception.AddressBookBusinessException;
import com.musclegrow.exception.OrderBusinessException;
import com.musclegrow.exception.ShoppingCartBusinessException;
import com.musclegrow.mapper.AddressBookMapper;
import com.musclegrow.mapper.OrderDetailMapper;
import com.musclegrow.mapper.OrderMapper;
import com.musclegrow.mapper.ShoppingCartMapper;
import com.musclegrow.mapper.UserMapper;
import com.musclegrow.mapper.VoucherMapper;
import com.musclegrow.mapper.VoucherStorageMapper;
import com.musclegrow.result.PageResult;
import com.musclegrow.service.OrderService;
import com.musclegrow.vo.OrderPaymentVO;
import com.musclegrow.vo.OrderStatisticsVO;
import com.musclegrow.vo.OrderSubmitVO;
import com.musclegrow.vo.OrderVO;
import com.musclegrow.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    private static final String MSG_VOUCHER_ORDER_REPEAT_NOT_SUPPORTED = "\u4f18\u60e0\u5238\u8ba2\u5355\u4e0d\u652f\u6301\u518d\u6765\u4e00\u5355";
    private static final String MSG_VOUCHER_NOT_FOUND = "\u4f18\u60e0\u5238\u4e0d\u5b58\u5728";
    private static final String MSG_VOUCHER_ALREADY_PURCHASED = "\u8be5\u4f18\u60e0\u5238\u6bcf\u4eba\u53ea\u80fd\u8d2d\u4e70\u4e00\u6b21";
    private static final String MSG_VOUCHER_NOT_AVAILABLE = "\u8be5\u4f18\u60e0\u5238\u5df2\u4e0b\u67b6\u3001\u8fc7\u671f\u6216\u5df2\u552e\u7f44";

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WebSocketServer webSocketServer;
    @Autowired
    private VoucherMapper voucherMapper;
    @Autowired
    private VoucherStorageMapper voucherStorageMapper;

    /**
     * 用户下单
     *
     * @param ordersSubmitDTO 提交订单参数
     * @return 下单结果
     */
    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        Long userId = getCurrentUserId();
        AddressBook addressBook = addressBookMapper.selectById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        List<ShoppingCart> shoppingCartList = listShoppingCartByUserId(userId);
        if (shoppingCartList.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        User user = userMapper.selectById(userId);
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setAddress(buildFullAddress(addressBook));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(userId);
        orders.setUserName(user != null ? user.getName() : null);
        orders.setMailAmount(ordersSubmitDTO.getPackAmount());
        orderMapper.insert(orders);

        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailMapper.insert(orderDetail);
        }

        clearShoppingCartByUserId(userId);

        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();
    }

    /**
     * 模拟订单支付
     *
     * @param ordersPaymentDTO 支付参数
     * @return 模拟支付结果
     */
    @Override
    @Transactional
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) {
        Long userId = getCurrentUserId();
        Orders orders = orderMapper.selectOne(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getNumber, ordersPaymentDTO.getOrderNumber())
                .eq(Orders::getUserId, userId)
                .last("limit 1"));

        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (Orders.PAID.equals(orders.getPayStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_ALREADY_PAID);
        }
        if (!Orders.PENDING_PAYMENT.equals(orders.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        completePaidOrder(orders, ordersPaymentDTO.getPayMethod());

        return OrderPaymentVO.builder()
                .nonceStr(orders.getNumber())
                .paySign("MOCK_PAY_SUCCESS")
                .timeStamp(String.valueOf(System.currentTimeMillis()))
                .signType("MOCK")
                .packageStr("mock-payment-success")
                .build();
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo 订单号
     */
    @Override
    @Transactional
    public void paySuccess(String outTradeNo) {
        Orders orders = orderMapper.selectOne(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getNumber, outTradeNo)
                .last("limit 1"));
        if (orders == null) {
            log.warn("支付成功回调未找到订单，orderNumber={}", outTradeNo);
            return;
        }
        if (!Orders.PENDING_PAYMENT.equals(orders.getStatus())) {
            log.info("订单已不是待支付状态，忽略重复支付回调，orderNumber={}, status={}", outTradeNo, orders.getStatus());
            return;
        }

        completePaidOrder(orders, orders.getPayMethod());
    }

    /**
     * 用户端订单分页查询
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param status   订单状态
     * @return 分页结果
     */
    @Override
    public PageResult pageQuery4User(int pageNum, int pageSize, Integer status) {
        Page<Orders> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId, getCurrentUserId());
        queryWrapper.eq(status != null, Orders::getStatus, status);
        queryWrapper.orderByDesc(Orders::getOrderTime);

        Page<Orders> resultPage = orderMapper.selectPage(page, queryWrapper);
        List<OrderVO> records = resultPage.getRecords().stream()
                .map(this::buildOrderVOWithDetails)
                .collect(Collectors.toList());
        return new PageResult(resultPage.getTotal(), records);
    }

    /**
     * 查询订单详情
     *
     * @param id 订单 id
     * @return 订单详情
     */
    @Override
    public OrderVO details(Long id) {
        Orders orders = getOrderOrThrow(id);
        return buildOrderVOWithDetails(orders);
    }

    /**
     * 用户端查询订单详情
     *
     * @param id 订单 id
     * @return 订单详情
     */
    @Override
    public OrderVO userDetails(Long id) {
        Orders orders = getUserOrderOrThrow(id);
        return buildOrderVOWithDetails(orders);
    }

    /**
     * 用户取消订单
     *
     * @param id 订单 id
     */
    @Override
    @Transactional
    public void userCancelById(Long id) {
        Orders orders = getUserOrderOrThrow(id);
        Integer status = orders.getStatus();

        if (Orders.PENDING_PAYMENT.equals(status)) {
            markOrderCancelled(orders, "用户取消订单", null, false);
            return;
        }
        if (Orders.TO_BE_CONFIRMED.equals(status)) {
            markOrderCancelled(orders, "用户取消订单", null, Orders.PAID.equals(orders.getPayStatus()));
            return;
        }
        if (Orders.CONFIRMED.equals(status) || Orders.DELIVERY_IN_PROGRESS.equals(status)) {
            throw new OrderBusinessException(MessageConstant.ORDER_CANCEL_REQUIRES_CONTACT);
        }
        throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
    }

    /**
     * 再来一单
     *
     * @param id 订单 id
     */
    @Override
    @Transactional
    public void repetition(Long id) {
        Orders orders = getUserOrderOrThrow(id);
        List<OrderDetail> orderDetailList = listOrderDetailsByOrderId(orders.getId());

        List<OrderDetail> repeatableOrderDetails = orderDetailList.stream()
                .filter(detail -> detail.getSupplementId() != null || detail.getSetmealId() != null)
                .collect(Collectors.toList());

        if (repeatableOrderDetails.isEmpty()) {
            throw new OrderBusinessException(MSG_VOUCHER_ORDER_REPEAT_NOT_SUPPORTED);
        }

        for (OrderDetail orderDetail : repeatableOrderDetails) {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart, "id");
            shoppingCart.setUserId(getCurrentUserId());
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    /**
     * 条件搜索订单
     *
     * @param ordersPageQueryDTO 搜索条件
     * @return 分页结果
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        Page<Orders> page = new Page<>(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.hasText(ordersPageQueryDTO.getNumber()), Orders::getNumber, ordersPageQueryDTO.getNumber());
        queryWrapper.like(StringUtils.hasText(ordersPageQueryDTO.getPhone()), Orders::getPhone, ordersPageQueryDTO.getPhone());
        queryWrapper.eq(ordersPageQueryDTO.getStatus() != null, Orders::getStatus, ordersPageQueryDTO.getStatus());
        queryWrapper.ge(ordersPageQueryDTO.getBeginTime() != null, Orders::getOrderTime, ordersPageQueryDTO.getBeginTime());
        queryWrapper.le(ordersPageQueryDTO.getEndTime() != null, Orders::getOrderTime, ordersPageQueryDTO.getEndTime());
        queryWrapper.orderByDesc(Orders::getOrderTime);

        Page<Orders> resultPage = orderMapper.selectPage(page, queryWrapper);
        List<OrderVO> orderVOList = resultPage.getRecords().stream()
                .map(this::buildOrderVOWithSummary)
                .collect(Collectors.toList());
        return new PageResult(resultPage.getTotal(), orderVOList);
    }

    /**
     * 各个状态的订单数量统计
     *
     * @return 订单统计
     */
    @Override
    public OrderStatisticsVO statistics() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(countOrdersByStatus(Orders.TO_BE_CONFIRMED));
        orderStatisticsVO.setConfirmed(countOrdersByStatus(Orders.CONFIRMED));
        orderStatisticsVO.setDeliveryInProgress(countOrdersByStatus(Orders.DELIVERY_IN_PROGRESS));
        return orderStatisticsVO;
    }

    /**
     * 商家接单
     *
     * @param ordersConfirmDTO 接单参数
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = getOrderOrThrow(ordersConfirmDTO.getId());
        if (!Orders.TO_BE_CONFIRMED.equals(orders.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        orderMapper.updateById(Orders.builder()
                .id(orders.getId())
                .status(Orders.CONFIRMED)
                .build());
    }

    /**
     * 商家拒单
     *
     * @param ordersRejectionDTO 拒单参数
     */
    @Override
    @Transactional
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        if (!StringUtils.hasText(ordersRejectionDTO.getRejectionReason())) {
            throw new OrderBusinessException(MessageConstant.ORDER_REJECTION_REASON_IS_NULL);
        }

        Orders orders = getOrderOrThrow(ordersRejectionDTO.getId());
        if (!Orders.TO_BE_CONFIRMED.equals(orders.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        markOrderCancelled(
                orders,
                null,
                ordersRejectionDTO.getRejectionReason().trim(),
                Orders.PAID.equals(orders.getPayStatus())
        );
    }

    /**
     * 商家取消订单
     *
     * @param ordersCancelDTO 取消参数
     */
    @Override
    @Transactional
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        if (!StringUtils.hasText(ordersCancelDTO.getCancelReason())) {
            throw new OrderBusinessException(MessageConstant.ORDER_CANCEL_REASON_IS_NULL);
        }

        Orders orders = getOrderOrThrow(ordersCancelDTO.getId());
        Integer status = orders.getStatus();
        if (!Orders.TO_BE_CONFIRMED.equals(status)
                && !Orders.CONFIRMED.equals(status)
                && !Orders.DELIVERY_IN_PROGRESS.equals(status)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        markOrderCancelled(
                orders,
                ordersCancelDTO.getCancelReason().trim(),
                null,
                Orders.PAID.equals(orders.getPayStatus())
        );
    }

    /**
     * 派送订单
     *
     * @param id 订单 id
     */
    @Override
    public void delivery(Long id) {
        Orders orders = getOrderOrThrow(id);
        if (!Orders.CONFIRMED.equals(orders.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        orderMapper.updateById(Orders.builder()
                .id(orders.getId())
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build());
    }

    /**
     * 完成订单
     *
     * @param id 订单 id
     */
    @Override
    public void complete(Long id) {
        Orders orders = getOrderOrThrow(id);
        if (!Orders.DELIVERY_IN_PROGRESS.equals(orders.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        orderMapper.updateById(Orders.builder()
                .id(orders.getId())
                .status(Orders.COMPLETED)
                .deliveryTime(LocalDateTime.now())
                .build());
    }

    /**
     * 客户催单
     *
     * @param id 订单 id
     */
    @Override
    public void reminder(Long id) {
        Orders orders = getUserOrderOrThrow(id);
        if (!Orders.TO_BE_CONFIRMED.equals(orders.getStatus())
                && !Orders.CONFIRMED.equals(orders.getStatus())
                && !Orders.DELIVERY_IN_PROGRESS.equals(orders.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        sendOrderNotification(2, orders.getId(), orders.getNumber());
    }

    @Override
    @Transactional
    public void processTimeoutOrders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime timeoutThreshold = now.minusMinutes(15);

        int affected = orderMapper.update(
                Orders.builder()
                        .status(Orders.CANCELLED)
                        .cancelReason("支付超时，自动取消")
                        .cancelTime(now)
                        .build(),
                new LambdaUpdateWrapper<Orders>()
                        .eq(Orders::getStatus, Orders.PENDING_PAYMENT)
                        .eq(Orders::getPayStatus, Orders.UN_PAID)
                        .le(Orders::getOrderTime, timeoutThreshold)
        );

        if (affected > 0) {
            log.info("处理支付超时订单完成，count={}", affected);
        }
    }

    @Override
    @Transactional
    public void processDeliveryOrders() {
        LocalDateTime now = LocalDateTime.now();

        int affected = orderMapper.update(
                Orders.builder()
                        .status(Orders.COMPLETED)
                        .deliveryTime(now)
                        .build(),
                new LambdaUpdateWrapper<Orders>()
                        .eq(Orders::getStatus, Orders.DELIVERY_IN_PROGRESS)
        );

        if (affected > 0) {
            log.info("处理派送中自动完成订单完成，count={}", affected);
        }
    }

    private Long getCurrentUserId() {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new OrderBusinessException(MessageConstant.USER_NOT_LOGIN);
        }
        return userId;
    }

    private Orders getOrderOrThrow(Long id) {
        Orders orders = orderMapper.selectById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        return orders;
    }

    private Orders getUserOrderOrThrow(Long id) {
        Orders orders = getOrderOrThrow(id);
        if (!getCurrentUserId().equals(orders.getUserId())) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        return orders;
    }

    private List<ShoppingCart> listShoppingCartByUserId(Long userId) {
        return shoppingCartMapper.selectList(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, userId)
                .orderByAsc(ShoppingCart::getCreateTime));
    }

    private void clearShoppingCartByUserId(Long userId) {
        shoppingCartMapper.delete(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, userId));
    }

    private List<OrderDetail> listOrderDetailsByOrderId(Long orderId) {
        return orderDetailMapper.selectList(new LambdaQueryWrapper<OrderDetail>()
                .eq(OrderDetail::getOrderId, orderId));
    }

    private String buildFullAddress(AddressBook addressBook) {
        List<String> segments = new ArrayList<>();
        if (StringUtils.hasText(addressBook.getProvinceName())) {
            segments.add(addressBook.getProvinceName());
        }
        if (StringUtils.hasText(addressBook.getCityName())) {
            segments.add(addressBook.getCityName());
        }
        if (StringUtils.hasText(addressBook.getDistrictName())) {
            segments.add(addressBook.getDistrictName());
        }
        if (StringUtils.hasText(addressBook.getDetail())) {
            segments.add(addressBook.getDetail());
        }
        return String.join("", segments);
    }

    private void markOrderPaid(Orders orders, Integer payMethod) {
        orderMapper.updateById(Orders.builder()
                .id(orders.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .payMethod(payMethod)
                .checkoutTime(LocalDateTime.now())
                .build());
        sendOrderNotification(1, orders.getId(), orders.getNumber());
    }

    private void completePaidOrder(Orders orders, Integer payMethod) {
        List<OrderDetail> orderDetailList = listOrderDetailsByOrderId(orders.getId());
        if (isVoucherOrder(orderDetailList)) {
            completeVoucherOrder(orders, orderDetailList.get(0), payMethod);
            return;
        }

        markOrderPaid(orders, payMethod);
    }

    private boolean isVoucherOrder(List<OrderDetail> orderDetailList) {
        return orderDetailList != null
                && orderDetailList.size() == 1
                && orderDetailList.get(0).getVoucherId() != null
                && orderDetailList.get(0).getSupplementId() == null
                && orderDetailList.get(0).getSetmealId() == null;
    }

    private void completeVoucherOrder(Orders orders, OrderDetail orderDetail, Integer payMethod) {
        Long voucherId = orderDetail.getVoucherId();
        Voucher voucher = voucherMapper.selectById(voucherId);
        if (voucher == null) {
            throw new OrderBusinessException(MSG_VOUCHER_NOT_FOUND);
        }

        Long count = voucherStorageMapper.selectCount(new LambdaQueryWrapper<VoucherStorage>()
                .eq(VoucherStorage::getUserId, orders.getUserId())
                .eq(VoucherStorage::getVoucherId, voucherId));
        if (count != null && count > 0) {
            throw new OrderBusinessException(MSG_VOUCHER_ALREADY_PURCHASED);
        }

        LocalDateTime now = LocalDateTime.now();
        int affected = voucherMapper.deductStock(voucherId, now);
        if (affected == 0) {
            throw new OrderBusinessException(MSG_VOUCHER_NOT_AVAILABLE);
        }

        orderMapper.updateById(Orders.builder()
                .id(orders.getId())
                .status(Orders.COMPLETED)
                .payStatus(Orders.PAID)
                .payMethod(payMethod)
                .checkoutTime(now)
                .deliveryTime(now)
                .build());

        voucherStorageMapper.insert(VoucherStorage.builder()
                .userId(orders.getUserId())
                .voucherId(voucherId)
                .orderId(null)
                .name(voucher.getTitle())
                .actualValue(voucher.getActualValue())
                .status(VoucherStorageStatusConstant.UNUSED)
                .createTime(now)
                .expireTime(voucher.getEndTime())
                .build());
    }

    private void markOrderCancelled(Orders orders, String cancelReason, String rejectionReason, boolean refund) {
        Orders updateOrder = Orders.builder()
                .id(orders.getId())
                .status(Orders.CANCELLED)
                .cancelReason(cancelReason)
                .rejectionReason(rejectionReason)
                .cancelTime(LocalDateTime.now())
                .payStatus(refund ? Orders.REFUND : orders.getPayStatus())
                .build();
        orderMapper.updateById(updateOrder);
    }

    private void sendOrderNotification(int type, Long orderId, String orderNumber) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", type);
        payload.put("orderId", orderId);
        payload.put("content", "订单号：" + orderNumber);
        webSocketServer.sendToAllClient(JSON.toJSONString(payload));
    }

    private OrderVO buildOrderVOWithDetails(Orders orders) {
        List<OrderDetail> orderDetailList = listOrderDetailsByOrderId(orders.getId());
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        orderVO.setOrderSupplements(buildOrderSummary(orderDetailList));
        return orderVO;
    }

    private OrderVO buildOrderVOWithSummary(Orders orders) {
        List<OrderDetail> orderDetailList = listOrderDetailsByOrderId(orders.getId());
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderSupplements(buildOrderSummary(orderDetailList));
        return orderVO;
    }

    private String buildOrderSummary(List<OrderDetail> orderDetailList) {
        return orderDetailList.stream()
                .map(detail -> {
                    StringBuilder summary = new StringBuilder(detail.getName());
                    if (StringUtils.hasText(detail.getSupplementDetail())) {
                        summary.append("(").append(detail.getSupplementDetail()).append(")");
                    }
                    summary.append("*").append(detail.getNumber());
                    return summary.toString();
                })
                .collect(Collectors.joining("; "));
    }

    private Integer countOrdersByStatus(Integer status) {
        Long count = orderMapper.selectCount(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getStatus, status));
        return count == null ? 0 : count.intValue();
    }
}
