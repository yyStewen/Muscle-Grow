package com.musclegrow.mq;

import com.musclegrow.config.RabbitMqConfiguration;
import com.musclegrow.entity.Orders;
import com.musclegrow.message.VoucherOrderCreateMessage;
import com.musclegrow.service.VoucherOrderAsyncService;
import com.musclegrow.service.VoucherSeckillRedisService;
import com.musclegrow.service.VoucherSeckillResultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class VoucherOrderConsumer {

    private static final String MSG_CREATE_FAILED = "\u79d2\u6740\u8ba2\u5355\u521b\u5efa\u5931\u8d25";

    @Autowired
    private VoucherOrderAsyncService voucherOrderAsyncService;

    @Autowired
    private VoucherSeckillRedisService voucherSeckillRedisService;

    @Autowired
    private VoucherSeckillResultService voucherSeckillResultService;

    @RabbitListener(queues = RabbitMqConfiguration.VOUCHER_ORDER_CREATE_QUEUE)
    public void onMessage(VoucherOrderCreateMessage message) {
        try {
            Orders order = voucherOrderAsyncService.createPendingVoucherOrder(message);
            voucherSeckillResultService.saveSuccess(message.getRequestId(), order.getId(), order.getNumber());
        } catch (Exception ex) {
            log.error("create voucher seckill order failed, requestId={}, voucherId={}, userId={}",
                    message.getRequestId(), message.getVoucherId(), message.getUserId(), ex);
            voucherSeckillRedisService.rollbackReservation(message.getVoucherId(), message.getUserId());
            voucherSeckillResultService.saveFailed(
                    message.getRequestId(),
                    ex.getMessage() == null || ex.getMessage().isEmpty() ? MSG_CREATE_FAILED : ex.getMessage()
            );
        }
    }
}
