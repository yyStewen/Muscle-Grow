package com.musclegrow.service.impl;

import com.musclegrow.config.RabbitMqConfiguration;
import com.musclegrow.message.VoucherOrderCreateMessage;
import com.musclegrow.service.VoucherOrderProducer;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VoucherOrderProducerImpl implements VoucherOrderProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void sendCreateOrderMessage(VoucherOrderCreateMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitMqConfiguration.VOUCHER_ORDER_EXCHANGE,
                RabbitMqConfiguration.VOUCHER_ORDER_CREATE_ROUTING_KEY,
                message
        );
    }
}
