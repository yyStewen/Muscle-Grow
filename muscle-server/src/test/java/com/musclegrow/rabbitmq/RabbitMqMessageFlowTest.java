package com.musclegrow.rabbitmq;

import com.musclegrow.MuscleGrowApplication;
import com.musclegrow.config.RabbitMqConfiguration;
import com.musclegrow.mq.VoucherOrderConsumer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@SpringBootTest(classes = MuscleGrowApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("dev")
@Import(RabbitMqMessageFlowTest.TestVoucherOrderListener.class)
class RabbitMqMessageFlowTest {

    @MockBean
    private ServerEndpointExporter serverEndpointExporter;

    @MockBean
    private VoucherOrderConsumer voucherOrderConsumer;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    private TestVoucherOrderListener testVoucherOrderListener;

    @BeforeEach
    void setUp() {
        rabbitAdmin.purgeQueue(RabbitMqConfiguration.VOUCHER_ORDER_CREATE_QUEUE, true);
        testVoucherOrderListener.reset();
    }

    @Test
    void shouldSendAndReceiveVoucherOrderCreateMessage() throws InterruptedException {
        VoucherOrderCreateTestMessage message = new VoucherOrderCreateTestMessage();
        message.setRequestId(UUID.randomUUID().toString());
        message.setUserId(7L);
        message.setVoucherId(3L);
        message.setCreatedTime(LocalDateTime.now().withSecond(0).withNano(0));

        rabbitTemplate.convertAndSend(
                RabbitMqConfiguration.VOUCHER_ORDER_EXCHANGE,
                RabbitMqConfiguration.VOUCHER_ORDER_CREATE_ROUTING_KEY,
                message
        );

        boolean received = testVoucherOrderListener.awaitMessage(5, TimeUnit.SECONDS);
        Assertions.assertTrue(received, "RabbitMQ listener did not receive the test message in time");

        VoucherOrderCreateTestMessage receivedMessage = testVoucherOrderListener.getReceivedMessage();
        Assertions.assertNotNull(receivedMessage);
        Assertions.assertEquals(message.getRequestId(), receivedMessage.getRequestId());
        Assertions.assertEquals(message.getUserId(), receivedMessage.getUserId());
        Assertions.assertEquals(message.getVoucherId(), receivedMessage.getVoucherId());
        Assertions.assertEquals(message.getCreatedTime(), receivedMessage.getCreatedTime());
    }

    static class VoucherOrderCreateTestMessage {
        private String requestId;
        private Long userId;
        private Long voucherId;
        private LocalDateTime createdTime;

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Long getVoucherId() {
            return voucherId;
        }

        public void setVoucherId(Long voucherId) {
            this.voucherId = voucherId;
        }

        public LocalDateTime getCreatedTime() {
            return createdTime;
        }

        public void setCreatedTime(LocalDateTime createdTime) {
            this.createdTime = createdTime;
        }
    }

    @Component
    static class TestVoucherOrderListener {
        private volatile VoucherOrderCreateTestMessage receivedMessage;
        private CountDownLatch countDownLatch = new CountDownLatch(1);

        @RabbitListener(queues = RabbitMqConfiguration.VOUCHER_ORDER_CREATE_QUEUE)
        public void onMessage(VoucherOrderCreateTestMessage message) {
            this.receivedMessage = message;
            this.countDownLatch.countDown();
        }

        void reset() {
            this.receivedMessage = null;
            this.countDownLatch = new CountDownLatch(1);
        }

        boolean awaitMessage(long timeout, TimeUnit timeUnit) throws InterruptedException {
            return countDownLatch.await(timeout, timeUnit);
        }

        VoucherOrderCreateTestMessage getReceivedMessage() {
            return receivedMessage;
        }
    }
}
