# Canal / RabbitMQ 功能 Bug 总结

## 1. Redis 补偿查询范围错误

### 现象

RabbitMQ 消费失败后，定时补偿任务长期查不到 Redis 中的待处理消息。

### 原因

原代码使用 `rangeByScore(key, time, time)`，只匹配分数恰好等于当前毫秒的记录，而消息写入时间几乎不可能与任务执行时间完全相同。

### 修复

改为查询 `0 <= score <= 当前时间 - 1分钟` 的全部记录。补发后只更新时间，不立即删除；由 Search 成功消费后删除，避免消息尚未消费就丢失补偿记录。

## 2. Search 的 Nacos 共享配置无法正确解析

### 现象

Search 可能无法加载 `application-dev.yml`，RabbitMQ 地址也可能退回默认的本机配置，导致无法消费 Canal 消息。

### 原因

`spring.cloud.nacos.config.file-extension` 被误写成了带空格的 `file-exte nsion`。

### 修复

修正配置键，并在 Search 本地配置中补充 RabbitMQ 集群地址、账号和密码；Nacos 的共享配置如存在同名项，应保持相同值。

## 3. 定时任务调用目标与实际 Bean 不一致

### 现象

启用或手动执行数据库中的任务时，出现找不到 `travelJob`、`redisJob` 或 `messageJob` Bean/方法的错误。

### 原因

SQL 初始化数据来自旧代码，当前任务类实际使用的是 `strategyTask`，方法名也已经发生变化。

### 修复

将任务目标更新为当前存在的 `testTask.test`、`strategyTask.statisRank`、`strategyTask.statisCondition`、`strategyTask.statisHashMapPersist` 和 `strategyTask.checkRabbitMQMessage`。

## 4. RabbitMQ 异常消息可能无限重复投递

### 现象

非法 JSON 或无法处理的消息会持续重新进入队列，影响后续正常消息。

### 原因

消费者没有空消息、非法 JSON 和缺少主键的校验，Rabbit 监听容器也没有明确的有限重试策略。

### 修复

消费者增加消息格式校验；监听容器增加 3 次重试和指数退避，最终拒绝且不再重新入队，避免坏消息阻塞队列。

## 5. 事务未提交就发送搜索消息

### 现象

数据库事务回滚时，Search 仍可能收到消息并生成不存在的 ES 文档；同时 Canal 提交后还会再发送一条变更消息。

### 原因

策略新增逻辑在事务提交前直接发送 RabbitMQ 消息。

### 修复

保留旧消息通道兼容能力，但将 Redis 记录和 RabbitMQ 发布移动到事务 `afterCommit` 阶段。Canal 仍负责数据库提交后的增量变更，ES 使用策略 ID 保证重复索引结果幂等。

## 验证建议

1. 启用 `strategyTask.checkRabbitMQMessage`，确认 Redis 中超过一分钟的记录会被补发。
2. 新增、修改、删除一条 `ta_strategy` 记录，确认 Canal 日志、RabbitMQ 消息和 ES 文档状态一致。
3. 在任务管理页面手动执行 `strategyTask.statisRank()`，确认不再出现 Bean 找不到错误。
4. 向 `travel-queue` 发送非法 JSON，确认最多重试 3 次后不再循环投递。
