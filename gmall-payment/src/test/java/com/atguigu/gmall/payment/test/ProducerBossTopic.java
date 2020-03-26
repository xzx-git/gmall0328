package com.atguigu.gmall.payment.test;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;

public class ProducerBossTopic {
    public static void main(String[] args) {

        //生成某一个人地址的连接池
        ConnectionFactory connect = new ActiveMQConnectionFactory("tcp://localhost:61616");
        try {
            //建立mq的连接
            Connection connection = connect.createConnection();
            connection.start();
            //通过连接创建一次与mq的会话任务
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Topic testqueue = session.createTopic("KAIHUI");

            //通过mq的会话任务将队列消息发送出去
            MessageProducer producer = session.createProducer(testqueue);
            TextMessage textMessage=new ActiveMQTextMessage();
            textMessage.setText("今天是阴天！");
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(textMessage);

            //任务的提交
            session.commit();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }


}
