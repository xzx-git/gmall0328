package com.atguigu.gmall.order.mq;

import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class OrderMqListener {

    @Autowired
    OrderService orderService;

    @JmsListener(containerFactory = "jmsQueueListener",destination = "PAYMENT_SUCCESS_QUEUE")
    public void consumePaymentSuccess(MapMessage mapMessage) throws JMSException {
        String trackingNo = mapMessage.getString("trackingNo");
        String outTradeNo = mapMessage.getString("outTradeNo");

        //修改订单状态
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfo.setTrackingNo(trackingNo);
        orderInfo.setOrderStatus("订单已支付");
        orderInfo.setProcessStatus("准备出库");
        orderService.updateOrderStatus(orderInfo);

        //发送订单的消息。给库存
        orderService.sendOrderResultQueue(orderInfo.getOutTradeNo());

        System.out.println("订单监听支付成功的监听器。。。trackingNo:"+trackingNo+"outTradeNo:"+outTradeNo);
    }
}
