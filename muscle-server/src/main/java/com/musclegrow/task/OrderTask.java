package com.musclegrow.task;

import com.musclegrow.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderService orderService;

    /**
     * 处理支付超时订单
     */
    @Scheduled(cron = "0 * * * * ?", zone = "Asia/Shanghai")
    public void processTimeoutOrders() {
        log.debug("开始处理支付超时订单");
        orderService.processTimeoutOrders();
    }

    /**
     * 
     * @Scheduled(cron = "0 0 1 * * ?", zone = "Asia/Shanghai")
     *                 public void processDeliveryOrders() {
     *                 log.debug("开始处理派送中自动完成订单");
     *                 orderService.processDeliveryOrders();
     *                 }
     */

}
