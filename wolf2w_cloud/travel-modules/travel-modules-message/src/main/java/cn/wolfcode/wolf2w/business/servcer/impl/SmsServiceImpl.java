package cn.wolfcode.wolf2w.business.servcer.impl;

import cn.wolfcode.wolf2w.business.servcer.ISmsService;
import cn.wolfcode.wolf2w.business.util.SmsUtil;
import cn.wolfcode.wolf2w.common.redis.service.RedisService;
import cn.wolfcode.wolf2w.common.redis.util.RedisKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class SmsServiceImpl implements ISmsService {

    @Autowired
    private RedisService RedisService;
    @Override
    public void sendVerifyCode(String phone) {

        // 生成四位验证码
        Random random = new Random();
        String verifyCode = String.format("%04d", random.nextInt(10000));
        // 发送验证码 --> 真实发送再打开
        // SmsUtil.sendSmsAliyun(phone,verifyCode);
        System.err.println("验证码："+verifyCode);

        // 缓存验证码(做一个key,value就是验证码)
        String key = RedisKeys.VERIFY_CODE.join(phone);
        RedisService.setCacheObject(key, verifyCode,RedisKeys.VERIFY_CODE.getExpire(), TimeUnit.SECONDS);
    }
}
