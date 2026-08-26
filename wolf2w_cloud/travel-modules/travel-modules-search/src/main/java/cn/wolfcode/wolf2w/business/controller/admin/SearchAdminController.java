package cn.wolfcode.wolf2w.business.controller.admin;

import cn.wolfcode.wolf2w.business.service.IDestinationESService;
import cn.wolfcode.wolf2w.business.service.INoteESService;
import cn.wolfcode.wolf2w.business.service.IStrategyESService;
import cn.wolfcode.wolf2w.business.service.IUserInfoESService;
import cn.wolfcode.wolf2w.common.core.domain.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

@RestController
public class SearchAdminController {

    @Autowired
    private IStrategyESService strategyService;
    @Autowired
    private IUserInfoESService userInfoESService;
    @Autowired
    private INoteESService noteESService;
    @Autowired
    private IDestinationESService destinationESService;

    @RequestMapping("/esInit")
    public R<Void> init() throws IOException, InvocationTargetException, IllegalAccessException {
        strategyService.init();
        userInfoESService.init();
        noteESService.init();
        destinationESService.init();
        System.out.println("初始化完成");
        return R.ok();
    }
}
