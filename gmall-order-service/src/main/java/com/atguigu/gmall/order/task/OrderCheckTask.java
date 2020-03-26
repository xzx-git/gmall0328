package com.atguigu.gmall.order.task;

import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class OrderCheckTask {

    @Autowired
    OrderService orderService;

    @Scheduled(cron = "0/10 * * * * ?")//每过十秒检查一下订单
    public void work() throws InterruptedException{

        //orderService.deleteOrderOverdue();


        System.out.println("定时检查过期订单，删除订单");
    }
}
