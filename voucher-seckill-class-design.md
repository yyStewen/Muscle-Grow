# Voucher Seckill Class Design

## 1. Design Goal

This document defines the class and interface design for coupon seckill implementation in the current project.

Design principles:

- reuse current `orders`, `order_detail`, and `voucher_storage`
- minimize changes to normal order logic
- isolate voucher seckill specific logic
- keep code structure consistent with the current service-controller-mapper layering

## 2. Package Layout

Recommended packages:

- `com.musclegrow.config`
- `com.musclegrow.constant`
- `com.musclegrow.controller.user`
- `com.musclegrow.message`
- `com.musclegrow.mq`
- `com.musclegrow.service`
- `com.musclegrow.service.impl`
- `com.musclegrow.vo`

## 3. New Constants

### 3.1 Redis Key Constant

File:

- `com.musclegrow.constant.VoucherSeckillRedisKeyConstant`

Suggested fields:

```java
public final class VoucherSeckillRedisKeyConstant {

    public static final String VOUCHER_STOCK = "voucher:stock:";
    public static final String VOUCHER_PENDING = "voucher:pending:";
    public static final String VOUCHER_BOUGHT = "voucher:bought:";
    public static final String VOUCHER_RESULT = "voucher:result:";
    public static final String VOUCHER_CONSUME = "voucher:consume:";
    public static final String VOUCHER_ORDER_NO = "voucher:order:no:";

    private VoucherSeckillRedisKeyConstant() {
    }
}
```

Recommended ttl rule:

- `voucher:pending:{voucherId}:{userId}` should use 20 minutes, which is slightly longer than the 15 minute unpaid order timeout.

### 3.2 Async Result Status Constant

File:

- `com.musclegrow.constant.VoucherSeckillResultStatusConstant`

Suggested fields:

```java
public final class VoucherSeckillResultStatusConstant {

    public static final String PROCESSING = "PROCESSING";
    public static final String SUCCESS = "SUCCESS";
    public static final String FAILED = "FAILED";

    private VoucherSeckillResultStatusConstant() {
    }
}
```

## 4. New VO Design

### 4.1 VoucherSeckillSubmitVO

File:

- `muscle-pojo/src/main/java/com/musclegrow/vo/VoucherSeckillSubmitVO.java`

Purpose:

- returned immediately after seckill request submission

Suggested structure:

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherSeckillSubmitVO implements Serializable {
    private String requestId;
    private String status;
    private String message;
}
```

### 4.2 VoucherSeckillResultVO

File:

- `muscle-pojo/src/main/java/com/musclegrow/vo/VoucherSeckillResultVO.java`

Purpose:

- returned by result polling API

Suggested structure:

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherSeckillResultVO implements Serializable {
    private String requestId;
    private String status;
    private Long orderId;
    private String orderNumber;
    private String message;
}
```

## 5. MQ Message Design

### 5.1 VoucherOrderCreateMessage

File:

- `com.musclegrow.message.VoucherOrderCreateMessage`

Suggested structure:

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherOrderCreateMessage implements Serializable {
    private String requestId;
    private Long userId;
    private Long voucherId;
    private String orderNumber;
    private BigDecimal payAmount;
    private LocalDateTime requestTime;
}
```

## 6. Redis Script Configuration

### 6.1 VoucherSeckillRedisConfiguration

File:

- `com.musclegrow.config.VoucherSeckillRedisConfiguration`

Purpose:

- load Lua script as `DefaultRedisScript<Long>`

Suggested methods:

```java
@Configuration
public class VoucherSeckillRedisConfiguration {

    @Bean
    public DefaultRedisScript<Long> voucherSeckillRedisScript() {
        // load classpath:lua/voucher-seckill.lua
    }
}
```

## 7. New Service Interfaces

### 7.1 VoucherSeckillRedisService

File:

- `com.musclegrow.service.VoucherSeckillRedisService`

Responsibility:

- atomic reservation
- release reservation
- payment confirmation marker update
- stock preload

Suggested interface:

```java
public interface VoucherSeckillRedisService {

    Long tryReserve(Long voucherId, Long userId);

    void preloadStock(Long voucherId, Integer stock);

    void rollbackReservation(Long voucherId, Long userId);

    void confirmPurchase(Long voucherId, Long userId);

    boolean hasBought(Long voucherId, Long userId);
}
```

### 7.2 VoucherSeckillResultService

File:

- `com.musclegrow.service.VoucherSeckillResultService`

Responsibility:

- manage async seckill result for front-end polling

Suggested interface:

```java
public interface VoucherSeckillResultService {

    void saveProcessing(String requestId);

    void saveSuccess(String requestId, Long orderId, String orderNumber);

    void saveFailed(String requestId, String message);

    VoucherSeckillResultVO getResult(String requestId);
}
```

### 7.3 VoucherOrderProducer

File:

- `com.musclegrow.service.VoucherOrderProducer`

Responsibility:

- publish async create-order message

Suggested interface:

```java
public interface VoucherOrderProducer {

    void sendCreateOrderMessage(VoucherOrderCreateMessage message);
}
```

### 7.4 VoucherOrderNumberService

File:

- `com.musclegrow.service.VoucherOrderNumberService`

Responsibility:

- generate unique voucher order number

Suggested interface:

```java
public interface VoucherOrderNumberService {

    String nextOrderNumber();
}
```

## 8. Existing Service Adjustment

### 8.1 UserVoucherService

Current interface:

- `listAvailable()`
- `purchase(Long voucherId)`

Recommended target:

```java
public interface UserVoucherService {

    List<UserVoucherVO> listAvailable();

    VoucherSeckillSubmitVO seckill(Long voucherId);

    VoucherSeckillResultVO getSeckillResult(String requestId);

    @Deprecated
    OrderSubmitVO purchase(Long voucherId);
}
```

Notes:

- keep `purchase` temporarily for compatibility
- new front-end should call `seckill`

### 8.2 UserVoucherServiceImpl

Main responsibility after refactor:

- validate basic voucher state from DB
- call Redis reservation
- generate `requestId` and `orderNumber`
- send MQ message
- return async submit result

Suggested methods:

```java
public class UserVoucherServiceImpl implements UserVoucherService {

    @Override
    public VoucherSeckillSubmitVO seckill(Long voucherId) {
    }

    @Override
    public VoucherSeckillResultVO getSeckillResult(String requestId) {
    }

    private Voucher loadSeckillVoucherOrThrow(Long voucherId) {
    }

    private String buildRequestId(Long userId, Long voucherId) {
    }
}
```

## 9. MQ Consumer Design

### 9.1 VoucherOrderConsumer

File:

- `com.musclegrow.mq.VoucherOrderConsumer`

Responsibility:

- consume create-order message
- deduct DB stock
- create unpaid order and detail
- write async result
- rollback Redis reservation if failed

Suggested structure:

```java
@Component
public class VoucherOrderConsumer {

    @RabbitListener(queues = RabbitMqConfiguration.VOUCHER_ORDER_CREATE_QUEUE)
    public void onMessage(VoucherOrderCreateMessage message) {
    }

    @Transactional
    public void createVoucherOrder(VoucherOrderCreateMessage message) {
    }
}
```

Core logic inside `createVoucherOrder`:

- idempotency check by `requestId`
- load voucher
- call `voucherMapper.deductStock(...)`
- insert `orders`
- insert `order_detail`
- write success result

Failure branch:

- rollback Redis reservation
- write failed result

## 10. Controller Design

### 10.1 VoucherController

Recommended additions:

```java
@PostMapping("/seckill/{id}")
public Result<VoucherSeckillSubmitVO> seckill(@PathVariable("id") Long id) {
}

@GetMapping("/seckill/result/{requestId}")
public Result<VoucherSeckillResultVO> getSeckillResult(@PathVariable String requestId) {
}
```

Transition strategy:

- keep `/purchase/{id}` temporarily
- document it as legacy path

## 11. Order Service Refactor Points

### 11.1 OrderServiceImpl.completeVoucherOrder

Current responsibility includes:

- duplicate purchase check
- DB stock deduction
- order completion
- insert voucher storage

Target responsibility after refactor:

- mark order paid
- mark order completed
- insert voucher storage
- update Redis bought marker
- delete pending marker

That means DB stock deduction must be removed from the payment phase.

### 11.2 OrderServiceImpl.userCancelById

Need adjustment:

- if unpaid voucher order is cancelled:
  - update order cancelled
  - DB stock plus 1
  - Redis stock plus 1
  - delete pending marker

### 11.3 OrderServiceImpl.processTimeoutOrders

Need adjustment:

- if timed out order is an unpaid voucher order:
  - update order cancelled
  - DB stock plus 1
  - Redis stock plus 1
  - delete pending marker

## 12. Mapper Adjustment

### 12.1 VoucherMapper

Keep current method:

```java
int deductStock(@Param("id") Long id, @Param("now") LocalDateTime now);
```

Add one new method:

```java
@Update("update voucher set stock = stock + 1 where id = #{id}")
int restoreStock(@Param("id") Long id);
```

This is used for timeout rollback and user cancel rollback.

### 12.2 Database Constraints

Recommended DDL adjustments before rollout:

```sql
create unique index uk_orders_number on orders(number);
create unique index uk_voucher_storage_user_voucher on voucher_storage(user_id, voucher_id);
```

Purpose:

- `uk_orders_number` prevents duplicate order creation under repeated consume or retry
- `uk_voucher_storage_user_voucher` is the final hard guard for one user one voucher

## 13. Resource Design

### 13.1 Lua Script File

File:

- `muscle-server/src/main/resources/lua/voucher-seckill.lua`

Input:

- voucher stock key
- voucher bought key
- voucher pending key
- user id

Output:

- `0`: success
- `1`: sold out
- `2`: already bought
- `3`: pending exists

## 14. Existing Admin Voucher Service Adjustment

### 14.1 VoucherServiceImpl

The admin voucher management flow must synchronize Redis stock and availability when:

- voucher is created
- voucher is updated
- voucher is started
- voucher is stopped

Suggested integration point:

- call `voucherSeckillRedisService.preloadStock(voucherId, stock)` when voucher becomes available for seckill
- delete or refresh Redis stock when voucher is stopped or stock is changed

## 15. Recommended Implementation Order

1. add constants, VOs, MQ message object
2. add Redis Lua support and reservation service
3. add seckill API and async result API
4. add MQ producer and consumer
5. refactor payment success for voucher order
6. refactor cancel and timeout rollback
7. add tests

## 16. Minimum Test Set

Recommended tests:

- Redis Lua duplicate request check
- Redis Lua sold-out check
- MQ consumer idempotency check
- consumer creates one unpaid order successfully
- payment success inserts one `voucher_storage`
- timeout cancel restores stock
- user cancel restores stock
