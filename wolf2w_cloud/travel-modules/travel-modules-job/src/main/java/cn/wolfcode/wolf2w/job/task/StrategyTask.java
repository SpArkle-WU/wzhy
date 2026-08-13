package cn.wolfcode.wolf2w.job.task;

import cn.wolfcode.wolf2w.business.api.RemoteStrategyRankService;
import cn.wolfcode.wolf2w.business.api.RemoteStrategyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("strategyTask")
public class StrategyTask {

    @Autowired
    private RemoteStrategyService remoteStrategyService;

    // 攻略排名任务
    public void statisRank(){
        System.out.println("攻略排行榜统计排名");
        // 跨服务调用远程服务,统计攻略排名,inner 表示内部调用,省去调用
        remoteStrategyService.statisRank("inner");
    }

    // 条件导航数据任务
    public void statisCondition() {
        System.out.println("攻略条件导航统计");
        remoteStrategyService.statisCondition("inner");
    }

    // 统计数据持久化到数据库任务
    public void statisHashMapPersist() {
        System.out.println("攻略数据持久化到数据库");
        // 跨服务调用远程服务,持久化攻略数据,inner 表示内部调用,省去调用
        remoteStrategyService.statisHashMapPersist("inner");
    }

}
