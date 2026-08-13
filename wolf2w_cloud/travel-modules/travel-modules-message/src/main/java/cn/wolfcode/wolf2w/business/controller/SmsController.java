package cn.wolfcode.wolf2w.business.controller;

import cn.wolfcode.wolf2w.business.servcer.ISmsService;
import cn.wolfcode.wolf2w.common.core.domain.R;
import cn.wolfcode.wolf2w.common.core.web.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sms")
public class SmsController extends BaseController {

    @Autowired
    private ISmsService smsService;

     @RequestMapping("/sendVerifyCode")
      public R<?> send(String phone) {
         smsService.sendVerifyCode(phone);
         return R.ok();
     }
}
