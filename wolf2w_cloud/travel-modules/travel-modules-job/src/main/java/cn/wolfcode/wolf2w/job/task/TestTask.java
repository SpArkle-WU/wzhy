package cn.wolfcode.wolf2w.job.task;

import org.springframework.stereotype.Component;

import java.util.Date;

@Component("testTask")
public class TestTask {

    public void test(Integer num,String str)
    {
        System.out.println("执行测试任务"+ new Date() + "\t"+ num + "\t"+ str);
    }
}
