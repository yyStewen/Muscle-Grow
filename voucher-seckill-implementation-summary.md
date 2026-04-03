# 优惠券秒杀功能实现梳理

## 1. 文档目的

本文档用于整理 `muscle-grow` 当前已经落地的优惠券秒杀高并发方案，包括：

- 当前后端已经实现的功能
- 秒杀下单相关业务逻辑
- Redis、RabbitMQ、MySQL 在链路中的职责
- 订单创建、支付、取消、超时回滚的状态流转
- 当前方案的可用范围、联调方式和待补风险

这份文档偏向“当前实现说明”，用于后续联调、排查问题和继续迭代开发。

## 2. 当前实现结论

当前优惠券秒杀链路已经具备基础可用性，可以完成以下流程：

- 用户发起秒杀请求
- Redis 原子校验库存和一人一单资格
- RabbitMQ 异步削峰
- 消费端创建待支付订单和订单明细
- 用户支付成功后直接完成优惠券订单
- 将已购买优惠券写入 `voucher_storage`
- 订单超时未支付或用户取消未支付订单时回滚库存

当前方案适合：

- 本地联调
- 接口联调
- 功能演示
- 后续前端接入

当前方案暂不建议直接视为“生产级最终版”，因为数据库唯一索引、完整幂等兜底和压测验证还需要继续补强。

## 3. 当前涉及的核心接口

### 3.1 用户端接口

- `GET /user/voucher/list`
  - 查询当前可展示给用户购买的优惠券
- `POST /user/voucher/seckill/{id}`
  - 发起秒杀请求
  - 返回 `requestId`
- `GET /user/voucher/seckill/result/{requestId}`
  - 查询秒杀异步结果
  - 成功后返回 `orderId` 和 `orderNumber`
- `POST /user/voucher/purchase/{id}`
  - 旧的同步购买接口
  - 当前为了兼容旧逻辑仍然保留

### 3.2 秒杀结果返回约定

`POST /user/voucher/seckill/{id}` 返回：

- `requestId`
- `status`
- `message`

`GET /user/voucher/seckill/result/{requestId}` 返回：

- `requestId`
- `status`
- `orderId`
- `orderNumber`
- `message`

状态值：

- `PROCESSING`：秒杀请求已提交，异步建单中
- `SUCCESS`：建单成功
- `FAILED`：建单失败或结果不存在

## 4. 当前实现的核心组件

### 4.1 Controller

- `muscle-server/src/main/java/com/musclegrow/controller/user/VoucherController.java`

职责：

- 暴露用户秒杀入口
- 暴露秒杀结果查询接口
- 保留旧的同步购买接口

### 4.2 Redis 相关

- `muscle-server/src/main/resources/lua/voucher-seckill.lua`
- `muscle-server/src/main/java/com/musclegrow/config/VoucherSeckillRedisConfiguration.java`
- `muscle-server/src/main/java/com/musclegrow/service/impl/VoucherSeckillRedisServiceImpl.java`
- `muscle-server/src/main/java/com/musclegrow/constant/VoucherSeckillRedisKeyConstant.java`

职责：

- 秒杀资格原子校验
- Redis 预扣库存
- 记录用户待支付占位
- 记录用户已购买集合
- 缓存异步结果
- 生成订单号递增序列

### 4.3 RabbitMQ 相关

- `muscle-server/src/main/java/com/musclegrow/config/RabbitMqConfiguration.java`
- `muscle-server/src/main/java/com/musclegrow/service/impl/VoucherOrderProducerImpl.java`
- `muscle-server/src/main/java/com/musclegrow/mq/VoucherOrderConsumer.java`
- `muscle-server/src/main/java/com/musclegrow/message/VoucherOrderCreateMessage.java`

职责：

- 将秒杀请求转为异步消息
- 削峰填谷
- 触发消费者异步创建订单

### 4.4 订单与支付相关

- `muscle-server/src/main/java/com/musclegrow/service/impl/UserVoucherServiceImpl.java`
- `muscle-server/src/main/java/com/musclegrow/service/impl/VoucherOrderAsyncServiceImpl.java`
- `muscle-server/src/main/java/com/musclegrow/service/impl/OrderServiceImpl.java`
- `muscle-server/src/main/java/com/musclegrow/mapper/VoucherMapper.java`

职责：

- 秒杀入口业务校验
- 异步创建待支付订单
- 支付成功后完成优惠券订单
- 取消和超时后回滚库存

## 5. Redis Key 设计

当前已经使用的 Redis key：

- `voucher:stock:{voucherId}`
  - 秒杀库存缓存
- `voucher:pending:{voucherId}:{userId}`
  - 用户针对该券的待支付占位
- `voucher:bought:{voucherId}`
  - 已购买该券的用户集合
- `voucher:result:{requestId}`
  - 秒杀异步处理结果
- `voucher:order:no:{yyyyMMdd}`
  - 当日订单号自增序列

### 5.1 pending 过期时间

`pending` key 当前设置为 20 分钟。

设计目的：

- 系统的未支付订单超时取消时间为 15 分钟
- `pending` 比订单超时稍长，便于异步建单和取消回滚留出缓冲时间

## 6. Lua 脚本业务逻辑

Lua 文件：

- `muscle-server/src/main/resources/lua/voucher-seckill.lua`

脚本做了 3 件事：

1. 判断用户是否已经在已购集合中
2. 判断用户是否已存在该券的待支付占位
3. 判断库存是否大于 0，并在成功时原子执行库存减 1 与写入待支付占位

当前返回值约定：

- `0`：预占成功
- `1`：库存不足
- `2`：用户已经买过
- `3`：用户已存在待支付订单

这一步是当前高并发处理最核心的一层，因为它把高并发校验前移到了 Redis 原子操作中。

## 7. 秒杀完整业务流转

### 7.1 秒杀入口流转

用户点击秒杀后，后端执行流程如下：

1. 从 `BaseContext` 获取当前登录用户 id
2. 查询数据库中的优惠券信息
3. 校验优惠券是否存在、是否上架、是否处于生效时间范围内
4. 查询数据库，判断用户是否已经购买过该券
5. 查询数据库，判断用户是否已存在该券的待支付订单
6. 将优惠券库存预热到 Redis
7. 执行 Lua 脚本进行原子预占
8. 如果预占成功，则生成：
   - `requestId`
   - 秒杀订单号 `orderNumber`
9. 将秒杀结果先写为 `PROCESSING`
10. 发送 RabbitMQ 建单消息
11. 接口立即返回，前端通过 `requestId` 轮询结果

### 7.2 MQ 消费建单流转

消费者收到消息后，执行如下流程：

1. 先按订单号查询 `orders`
2. 如果已存在，直接返回已有订单，避免重复建单
3. 查询优惠券
4. 通过 SQL 条件更新扣减数据库库存
5. 插入 `orders`
6. 插入 `order_detail`
7. 将异步结果更新为 `SUCCESS`

如果任意一步失败：

- 回滚 Redis 预占库存
- 删除用户待支付占位
- 将异步结果写为 `FAILED`

### 7.3 用户查询秒杀结果

前端轮询 `GET /user/voucher/seckill/result/{requestId}`：

- `PROCESSING`
  - 表示 MQ 尚未完成建单
- `SUCCESS`
  - 表示订单已创建成功
  - 可拿到 `orderId` 和 `orderNumber`
- `FAILED`
  - 表示秒杀失败或结果已过期

## 8. 秒杀订单的数据落库设计

### 8.1 使用到的表

- `voucher`
  - 优惠券主表
- `orders`
  - 优惠券订单主表
- `order_detail`
  - 优惠券订单明细表
- `voucher_storage`
  - 用户已持有优惠券仓库表

### 8.2 秒杀订单创建时写入的数据

当消费者成功建单时：

- `orders`
  - `status = PENDING_PAYMENT`
  - `pay_status = UN_PAID`
  - `remark = 优惠券秒杀订单`
- `order_detail`
  - 记录 `voucher_id`
  - `number = 1`

这意味着：秒杀成功并不等于用户已经持有优惠券，真正持有优惠券是在支付成功之后。

## 9. 支付成功后的业务逻辑

当前优惠券秒杀订单采用“支付后直接完成”的规则。

### 9.1 支付成功处理

当用户对秒杀优惠券订单支付成功时：

1. 查询订单明细，识别这是一个优惠券订单
2. 再判断它是否为“优惠券秒杀订单”
3. 如果是秒杀订单，则不再二次扣库存
4. 直接更新订单：
   - `status = COMPLETED`
   - `pay_status = PAID`
5. 向 `voucher_storage` 插入一条记录
6. 将用户加入 Redis 的已购集合
7. 删除用户该券的 `pending` 占位

### 9.2 为什么秒杀支付时不再扣库存

因为秒杀链路中，库存已经在“建单阶段”通过数据库条件更新扣减过一次。

如果支付时再扣一次，就会出现重复扣库存的问题。

## 10. 取消与超时回滚逻辑

### 10.1 用户取消未支付秒杀订单

如果用户取消的是未支付的秒杀优惠券订单：

1. 订单状态改为 `CANCELLED`
2. 数据库库存加 1
3. Redis 库存加 1
4. 删除 Redis `pending` 占位

这样用户后续还可以重新抢该券。

### 10.2 未支付超时自动取消

系统已有订单超时任务，逻辑为：

- 每分钟检查一次
- 找出下单 15 分钟仍未支付的订单

如果这些订单是秒杀优惠券订单，则在取消订单后执行：

- 数据库库存恢复
- Redis 预占回滚

### 10.3 已支付后的订单

当前秒杀优惠券订单一旦支付成功，就直接进入 `COMPLETED`。

因此：

- 不会进入“待接单”
- 不会进入“派送中”
- 不存在餐饮订单那套商家接单流转

优惠券秒杀订单和普通商品订单在订单状态流转上是两条不同的业务线。

## 11. 一人一单是如何实现的

当前实现的一人一单，实际上由多层共同保障：

### 11.1 Redis 层

- `voucher:bought:{voucherId}`
  - 限制已经完成购买的用户再次秒杀
- `voucher:pending:{voucherId}:{userId}`
  - 限制用户在未支付订单存在期间重复秒杀

### 11.2 数据库层

在秒杀入口前，会先查数据库：

- 用户是否已经在 `voucher_storage` 中拥有该券
- 用户是否已有该券的待支付订单

### 11.3 建议补充的硬约束

建议在数据库增加唯一索引：

```sql
create unique index uk_orders_number on orders(number);
create unique index uk_voucher_storage_user_voucher on voucher_storage(user_id, voucher_id);
```

作用：

- `uk_orders_number`
  - 防止 MQ 重复消费导致重复建单
- `uk_voucher_storage_user_voucher`
  - 防止同一用户重复持有同一张优惠券

## 12. 防止超卖是如何实现的

当前防超卖由两层完成：

### 12.1 Redis 预校验层

Lua 脚本中先检查 Redis 库存是否大于 0。

优点：

- 快速失败
- 把大量无效请求挡在数据库之前

### 12.2 MySQL 最终扣减层

真正落库时通过条件更新扣减：

```sql
update voucher
set stock = stock - 1
where id = ?
  and stock > 0
  and status = 1
  and begin_time <= now
  and end_time >= now
```

优点：

- 即使 Redis 出现极端并发竞争，数据库仍然是最后一道库存正确性保障

## 13. 订单号设计

当前秒杀订单号由 Redis 自增序列生成。

格式：

- 前缀 `VO`
- 当前时间 `yyyyMMddHHmmss`
- 6 位自增序列

示例：

- `VO20260321223015000001`

作用：

- 便于分布式环境生成唯一订单号
- 作为 MQ 消费端幂等判断的一层依据

## 14. 优惠券管理与 Redis 库存同步

当前在管理端对优惠券进行新增、修改、上下架时，已经加入 Redis 库存同步逻辑：

- 优惠券处于投放中时，刷新 Redis 库存
- 优惠券下架或不可售时，删除 Redis 库存 key

这样可以尽量保证管理端维护的数据和秒杀链路使用的 Redis 库存保持一致。

## 15. 当前保留的旧逻辑

当前仍然保留旧的同步购买接口：

- `POST /user/voucher/purchase/{id}`

这个接口的特点：

- 不走 Redis 秒杀预占
- 不走 RabbitMQ 异步建单
- 下单时同步创建订单
- 支付时再扣库存

保留它的原因：

- 避免当前已有功能或前端直接断裂
- 便于对比新老两套流程

后续如果确定全面切换到秒杀链路，可以再决定是否下线旧接口。

## 16. 当前已验证情况

当前已经验证通过的内容：

- `mvn -pl muscle-server -am -DskipTests compile`
  - 编译通过
- `mvn -pl muscle-server -am -Dtest=RabbitMqMessageFlowTest -DfailIfNoTests=false test`
  - RabbitMQ 消息发送与监听测试通过

说明：

- 当前已确认 MQ 基础收发正常
- 但还没有完成完整业务链路压测
- 也还没有完成完整端到端自动化测试

## 17. 当前仍需补强的点

### 17.1 数据库唯一索引还未落库

建议尽快补上：

- `orders(number)` 唯一索引
- `voucher_storage(user_id, voucher_id)` 唯一索引

### 17.2 消费端幂等还可以继续加强

当前消费端主要依赖：

- `orders.number` 查询

这已经有一定效果，但如果后续进入更严格的高并发场景，仍建议增加更强的消费幂等标记机制。

### 17.3 缺少完整业务测试

建议继续补以下测试：

- 秒杀成功建单测试
- 秒杀失败回滚测试
- 支付成功发券测试
- 订单超时取消回滚测试
- 同一用户重复请求测试

### 17.4 还没有做真正压测

当前方案已经具备“高并发处理架构”，但还没有经过真实压测结果验证。

所以目前更准确的表述应是：

- 已具备高并发方案
- 已完成基础实现
- 已具备联调可用性
- 尚未完成生产级压测验收

## 18. 前端接入建议

用户端若要接入秒杀优惠券，应按如下顺序调用：

1. 调用 `POST /user/voucher/seckill/{id}`
2. 获取 `requestId`
3. 轮询 `GET /user/voucher/seckill/result/{requestId}`
4. 如果返回 `SUCCESS`，跳转订单支付
5. 调用现有订单支付接口
6. 支付成功后刷新用户优惠券列表

前端要特别注意：

- 秒杀接口不是同步返回订单
- 必须通过 `requestId` 轮询结果

## 19. 当前落地方案的总结

当前优惠券秒杀方案已经从原来的“同步建单、支付时扣库存”改造为：

- Redis 原子预占资格和库存
- RabbitMQ 异步建单削峰
- MySQL 条件扣减作为最终库存保护
- 支付成功直接完成优惠券订单并发券
- 未支付取消和超时自动回滚库存

从实现阶段来看，这一版已经不是单纯的设计稿，而是已经进入“可联调、可继续完善”的状态。

如果后续继续推进，建议优先顺序如下：

1. 落数据库唯一索引
2. 做完整端到端手工联调
3. 补业务测试
4. 再做压测和幂等增强
