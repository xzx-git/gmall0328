package com.atguigu.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.enums.OrderDetail;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.util.ActiveMQUtil;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    OrderInfoMapper orderInfoMapper;

    @Autowired
    OrderDetailMapper orderDetailMapper;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Override
    public String getTradeCode(String userId) {

        String k = "user:"+userId+":tradeCode";
        String v = UUID.randomUUID().toString();

        Jedis jedis = redisUtil.getJedis();
        jedis.setex(k,60*30,v);

        jedis.close();
        return v;
    }

    @Override
    public boolean checkTradeCode(String tradeCode, String userId) {
        boolean b = false;
        String k = "user:"+userId+":tradeCode";
        Jedis jedis = redisUtil.getJedis();
        String s = jedis.get(k);
        if (StringUtils.isNotBlank(s)&&s.equals(tradeCode)){
            b = true;
            jedis.del(k);
        }

        return b;
    }

    @Override
    public String saveOrder(OrderInfo orderInfo) {
        orderInfoMapper.insertSelective(orderInfo);
        String orderId = orderInfo.getId();
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderId);
            orderDetailMapper.insertSelective(orderDetail);
        }
        return orderInfo.getId();
    }

    @Override
    public OrderInfo getOrderById(String orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orderId);

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderId);
        List<OrderDetail> select = orderDetailMapper.select(orderDetail);
        orderInfo.setOrderDetailList(select);
        return orderInfo;
    }

    @Override
    public void updateOrderStatus(OrderInfo orderInfo) {
        Example example = new Example(OrderInfo.class);
        example.createCriteria().andEqualTo("outTradeNo",orderInfo.getOutTradeNo());
        orderInfoMapper.updateByExampleSelective(orderInfo,example);
    }

    @Override
    public void sendOrderResultQueue(String outTradeNo) {

        try {
            //建立mq的连接
            Connection connection = activeMQUtil.getConnection();
            connection.start();
            //通过连接创建一次与mq的会话任务
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue testqueue = session.createQueue("ORDER_RESULT_QUEUE");

            //通过mq的会话任务将队列消息发送出去
            MessageProducer producer = session.createProducer(testqueue);

            ActiveMQTextMessage textMessage = new ActiveMQTextMessage();

            //获得订单消息数据
            OrderInfo orderInfo = new OrderInfo();
            orderInfo.setOutTradeNo(outTradeNo);
            orderInfo = orderInfoMapper.selectOne(orderInfo);

            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderInfo.getId());
            List<OrderDetail> select = orderDetailMapper.select(orderDetail);
            orderInfo.setOrderDetailList(select);

            //将消息树转化为json字符串文本输出
            textMessage.setText(JSON.toJSONString(orderInfo));

            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(textMessage);

            //任务的提交
            session.commit();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }
        System.out.println("订单支付成功，发送订单服务的消息队列");
    }

    @Override
    public void deleteOrderOverdue() {
        List<OrderInfo> orderInfos = orderInfoMapper.selectAll();
        Date date = new Date();


        for (OrderInfo orderInfo : orderInfos) {
            int i = date.compareTo(orderInfo.getCreateTime());
            if (i==1){
                orderInfoMapper.deleteByPrimaryKey(orderInfo.getId());
            }
        }
    }
}
