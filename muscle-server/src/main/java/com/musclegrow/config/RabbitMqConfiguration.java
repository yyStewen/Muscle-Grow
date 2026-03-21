package com.musclegrow.config;

import com.musclegrow.json.JacksonObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMqConfiguration {

    public static final String VOUCHER_ORDER_EXCHANGE = "voucher.order.exchange";
    public static final String VOUCHER_ORDER_CREATE_QUEUE = "voucher.order.create.queue";
    public static final String VOUCHER_ORDER_CREATE_ROUTING_KEY = "voucher.order.create";

    @Bean
    public DirectExchange voucherOrderExchange() {
        return new DirectExchange(VOUCHER_ORDER_EXCHANGE, true, false);
    }

    @Bean
    public Queue voucherOrderCreateQueue() {
        return QueueBuilder.durable(VOUCHER_ORDER_CREATE_QUEUE).build();
    }

    @Bean
    public Binding voucherOrderCreateBinding(Queue voucherOrderCreateQueue, DirectExchange voucherOrderExchange) {
        return BindingBuilder.bind(voucherOrderCreateQueue)
                .to(voucherOrderExchange)
                .with(VOUCHER_ORDER_CREATE_ROUTING_KEY);
    }

    @Bean
    public MessageConverter rabbitMqMessageConverter() {
        return new Jackson2JsonMessageConverter(new JacksonObjectMapper());
    }
}
