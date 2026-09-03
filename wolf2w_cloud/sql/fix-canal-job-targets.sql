-- 将已有数据库中的旧任务目标同步到当前实际 Bean。
UPDATE sys_job
SET invoke_target = CASE job_id
    WHEN 104 THEN 'testTask.test(1, ''canal-test'')'
    WHEN 105 THEN 'strategyTask.statisRank'
    WHEN 106 THEN 'strategyTask.statisCondition'
    WHEN 108 THEN 'strategyTask.statisHashMapPersist'
    WHEN 109 THEN 'strategyTask.checkRabbitMQMessage'
    ELSE invoke_target
END,
job_name = CASE job_id
    WHEN 106 THEN 'condition统计任务'
    WHEN 109 THEN 'RabbitMQ消息补偿任务'
    ELSE job_name
END
WHERE job_id IN (104, 105, 106, 108, 109);
