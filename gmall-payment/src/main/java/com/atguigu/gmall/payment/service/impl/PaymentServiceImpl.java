package com.atguigu.gmall.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.payment.service.PaymentService;
import com.atguigu.gmall.util.ActiveMQUtil;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    PaymentInfoMapper paymentInfoMapper;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Autowired
    AlipayClient alipayClient;

    @Override
    public void savePayment(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);
    }

    @Override
    public void updatePayment(PaymentInfo paymentInfo) {
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo",paymentInfo.getOutTradeNo());

        paymentInfoMapper.updateByExampleSelective(paymentInfo,example);
    }

    @Override
    public void sendPaymentSuccessQueue(String tradeNo, String outTradeNo,String callbackContent) {

        //修改支付信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setPaymentStatus("已支付");
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setOutTradeNo(outTradeNo);
        paymentInfo.setCallbackContent(callbackContent);
        paymentInfo.setAlipayTradeNo(tradeNo);
        updatePayment(paymentInfo);

        try {
            //建立mq的连接
            Connection connection = activeMQUtil.getConnection();
            connection.start();
            //通过连接创建一次与mq的会话任务
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue testqueue = session.createQueue("PAYMENT_RESULT_QUEUE");

            //通过mq的会话任务将队列消息发送出去
            MessageProducer producer = session.createProducer(testqueue);
            ActiveMQMapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("trackingNo",tradeNo);
            mapMessage.setString("outTradeNo",outTradeNo);

            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(mapMessage);

            //任务的提交
            session.commit();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void sendPaymentCheckQueue(String outTradeNo, int count) {

        try {
            //建立mq的连接
            Connection connection = activeMQUtil.getConnection();
            connection.start();
            //通过连接创建一次与mq的会话任务
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue testqueue = session.createQueue("PAYMENT_CHECK_QUEUE");

            //通过mq的会话任务将队列消息发送出去
            MessageProducer producer = session.createProducer(testqueue);
            ActiveMQMapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setInt("count",count);
            mapMessage.setString("outTradeNo",outTradeNo);
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,1000*60);

            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(mapMessage);

            //任务的提交
            session.commit();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }
        System.out.println("发送弟"+(6-count)+"次的消息队列");
    }

    @Override
    public Map<String, String> checkPayment(String outTradeNo) {
        //调用支付宝的联查接口

        HashMap<String, String> stringStringHashMap = new HashMap<>();

        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();

        HashMap<String, Object> stringObjectHashMap = new HashMap<>();
        stringObjectHashMap.put("out_trade_no",outTradeNo);
        String s = JSON.toJSONString(stringObjectHashMap);
        request.setBizContent(s);
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){
            String tradeStatus = response.getTradeStatus();
            String status = tradeStatus;
            String callbackContent = response.getBody();
            String tradeNo = response.getTradeNo();
            stringStringHashMap.put("status",status);
            stringStringHashMap.put("callbackContent",callbackContent);
            stringStringHashMap.put("tradeNo",tradeNo);

        } else {
            System.out.println("用户未扫码");
        }
        return stringStringHashMap;
    }

    @Override
    public boolean checkPaied(String outTradeNo) {
        boolean b = false;

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(outTradeNo);
        PaymentInfo paymentInfo1 = paymentInfoMapper.selectOne(paymentInfo);
        if (paymentInfo1!=null&&paymentInfo1.getPaymentStatus().equals("已支付")){
            b = true;
        }
        return b;
    }
}
