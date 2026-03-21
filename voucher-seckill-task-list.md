# Voucher Seckill Task List

## 1. Goal

Build an implementable coupon seckill path for the current `muscle-grow` project using:

- Redis atomic reservation
- RabbitMQ async order creation
- MySQL final stock guard
- current mock payment flow

This task list is ordered by delivery priority.

## 2. Phase Breakdown

### Phase 0: Baseline Confirmation

Goal:

- make sure RabbitMQ, Redis, MySQL, and current voucher purchase flow are available before refactoring

Tasks:

- verify RabbitMQ config in `application.yml` and `application-dev.yml`
- verify Redis is reachable
- keep `RabbitMqMessageFlowTest` green
- manually verify current voucher order payment flow once

Output:

- stable local environment
- one green RabbitMQ integration test

Acceptance:

- application starts successfully
- RabbitMQ test passes
- existing voucher list API still works

### Phase 1: Redis Seckill Foundation

Goal:

- move high-concurrency validation to Redis

Tasks:

- add Redis key constants
- add Lua script file under `src/main/resources/lua`
- add Redis script loader bean
- add voucher seckill reservation service
- add voucher order number generator
- preload voucher stock to Redis when voucher goes on sale
- synchronize Redis stock when voucher stock or status changes on the admin side
- define async result cache format

Output:

- Redis can atomically decide whether the user gets a seckill qualification

Acceptance:

- one request can reserve stock successfully
- repeated request by same user returns duplicate result
- when stock reaches zero, new requests fail directly in Redis
- Redis `pending` ttl is longer than the 15 minute order timeout

### Phase 2: MQ Producer Side

Goal:

- convert successful Redis reservation into an async create-order message

Tasks:

- add `VoucherOrderCreateMessage`
- add `VoucherOrderProducer`
- add message send wrapper with fail rollback
- add service method to write `PROCESSING` result cache
- add controller endpoint `POST /user/voucher/seckill/{id}`

Output:

- seckill endpoint returns `requestId`
- MQ receives a serialized create-order message

Acceptance:

- producer sends message successfully
- MQ send failure triggers Redis rollback
- front-end receives `requestId` and processing state

### Phase 3: MQ Consumer Order Creation

Goal:

- create unpaid voucher orders asynchronously

Tasks:

- add `VoucherOrderConsumer`
- add consumer idempotency check by `requestId`
- move DB stock deduction to consumer
- create `orders` row with `PENDING_PAYMENT`
- create `order_detail` row with `voucherId`
- write seckill success result cache with `orderId` and `orderNumber`
- write failure result cache when DB creation fails

Output:

- unpaid voucher order is created by consumer instead of synchronous service code

Acceptance:

- duplicate MQ message does not create duplicate orders
- order is created only when DB stock deduction succeeds
- failure branch releases Redis reservation
- database unique indexes can block accidental duplicate persistence

### Phase 4: Payment Flow Refactor

Goal:

- stop deducting voucher stock during payment

Tasks:

- refactor `OrderServiceImpl.completeVoucherOrder`
- payment success only updates order state and inserts `voucher_storage`
- delete voucher pending marker after payment success
- add user into Redis bought marker after payment success

Output:

- payment only completes voucher ownership, no duplicate stock deduction

Acceptance:

- paid voucher order becomes `COMPLETED`
- `voucher_storage` is inserted exactly once
- Redis bought marker is updated

### Phase 5: Cancel and Timeout Rollback

Goal:

- release reserved stock if pending voucher order is not paid

Tasks:

- update user cancel branch for voucher pending orders
- update timeout task for voucher pending orders
- add rollback method:
  - DB stock plus one
  - Redis stock plus one
  - delete pending marker
- keep current normal meal order cancel logic unchanged

Output:

- voucher reservation is recoverable

Acceptance:

- timeout cancel returns one stock correctly
- user cancel of unpaid voucher order returns one stock correctly
- cancelled voucher order cannot issue voucher_storage

### Phase 6: Result Query API and Front-End Contract

Goal:

- allow the front-end to query async seckill result

Tasks:

- add `GET /user/voucher/seckill/result/{requestId}`
- return one of:
  - processing
  - success with `orderId` and `orderNumber`
  - failed with message
- define front-end polling strategy
- keep old purchase API for transition only

Output:

- front-end can wait for async order creation and then navigate to payment

Acceptance:

- polling stops on success or failed
- successful result can continue payment flow

### Phase 7: Tests and Stability

Goal:

- verify the new seckill path under repeated and concurrent requests

Tasks:

- add Redis Lua unit/integration tests
- add MQ consumer tests
- add payment success voucher issue test
- add timeout rollback test
- add duplicate user request test
- add simple concurrency verification script or manual test plan

Output:

- baseline regression confidence

Acceptance:

- no oversell
- no duplicate voucher ownership
- no duplicate pending orders for same user and same voucher

## 3. File Touch Plan

### New Back-End Classes

- `muscle-server/src/main/java/com/musclegrow/config/VoucherSeckillRedisConfiguration.java`
- `muscle-server/src/main/java/com/musclegrow/constant/VoucherSeckillRedisKeyConstant.java`
- `muscle-server/src/main/java/com/musclegrow/constant/VoucherSeckillResultStatusConstant.java`
- `muscle-server/src/main/java/com/musclegrow/message/VoucherOrderCreateMessage.java`
- `muscle-server/src/main/java/com/musclegrow/service/VoucherSeckillRedisService.java`
- `muscle-server/src/main/java/com/musclegrow/service/impl/VoucherSeckillRedisServiceImpl.java`
- `muscle-server/src/main/java/com/musclegrow/service/VoucherOrderProducer.java`
- `muscle-server/src/main/java/com/musclegrow/service/impl/VoucherOrderProducerImpl.java`
- `muscle-server/src/main/java/com/musclegrow/service/VoucherSeckillResultService.java`
- `muscle-server/src/main/java/com/musclegrow/service/impl/VoucherSeckillResultServiceImpl.java`
- `muscle-server/src/main/java/com/musclegrow/service/VoucherOrderNumberService.java`
- `muscle-server/src/main/java/com/musclegrow/service/impl/VoucherOrderNumberServiceImpl.java`
- `muscle-server/src/main/java/com/musclegrow/mq/VoucherOrderConsumer.java`
- `muscle-server/src/main/resources/lua/voucher-seckill.lua`

### Modified Back-End Classes

- `muscle-server/src/main/java/com/musclegrow/controller/user/VoucherController.java`
- `muscle-server/src/main/java/com/musclegrow/service/UserVoucherService.java`
- `muscle-server/src/main/java/com/musclegrow/service/impl/UserVoucherServiceImpl.java`
- `muscle-server/src/main/java/com/musclegrow/service/impl/OrderServiceImpl.java`
- `muscle-server/src/main/java/com/musclegrow/service/impl/VoucherServiceImpl.java`
- `muscle-server/src/main/java/com/musclegrow/task/OrderTask.java`
- `muscle-server/src/main/java/com/musclegrow/mapper/VoucherMapper.java`

### New VO Objects

- `muscle-pojo/src/main/java/com/musclegrow/vo/VoucherSeckillSubmitVO.java`
- `muscle-pojo/src/main/java/com/musclegrow/vo/VoucherSeckillResultVO.java`

## 4. Suggested Commit Points

- commit 1:
  - Redis key constants
  - Lua script
  - Redis reservation service
- commit 2:
  - MQ producer
  - seckill endpoint
  - async result query API
- commit 3:
  - MQ consumer
  - async order creation
- commit 4:
  - payment refactor
  - voucher issue logic
- commit 5:
  - timeout rollback
  - cancel rollback
- commit 6:
  - tests
  - cleanup old purchase path

## 5. Risks and Notes

- Do not switch the front-end directly to the new async API before the result polling API is ready.
- Do not remove the old `purchase` method before payment and timeout rollback logic are complete.
- Make sure voucher stock preload and status update are coordinated when vouchers are published or edited.
- Add database unique indexes before pressure testing.
- Keep normal meal order flow isolated from voucher seckill flow to reduce regression risk.

## 6. Done Definition

The feature is considered complete only when all items below are true:

- Redis can block duplicate seckill requests before DB access
- MQ consumer creates one unpaid order only once
- DB stock never becomes negative
- paid voucher orders issue exactly one voucher ownership record
- timeout and cancel both release stock correctly
- front-end can poll and continue payment without using the old synchronous coupon purchase path
