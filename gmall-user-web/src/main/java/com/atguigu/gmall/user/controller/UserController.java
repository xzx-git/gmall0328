package com.atguigu.gmall.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    @Reference
    UserService userService;

    @RequestMapping("userInfoList")
    public ResponseEntity<List<UserInfo>> userInfoList(){
        List<UserInfo> userInfoList = userService.userInfoList();
        return ResponseEntity.ok(userInfoList);
    }
}
