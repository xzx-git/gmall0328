package com.atguigu.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import com.atguigu.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserInfoMapper userInfoMapper;

    @Autowired
    UserAddressMapper userAddressMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public List<UserInfo> userInfoList() {
        return userInfoMapper.selectAll();
    }

    @Override
    public UserInfo login(UserInfo userInfo) {
        UserInfo user = userInfoMapper.selectOne(userInfo);
        if (user != null){
            Jedis jedis = redisUtil.getJedis();
            jedis.set("user:"+user.getId()+":info", JSON.toJSONString(user));
            jedis.close();
        }
        return user;
    }

    public UserAddress getUserAddressByAddressId(String deliveryAddress){
        UserAddress userAddress = new UserAddress();
        userAddress.setId(deliveryAddress);
        return userAddressMapper.selectOne(userAddress);
    }

    @Override
    public List<UserAddress> getUserAddressList(String userId) {
        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);
        List<UserAddress> select = userAddressMapper.select(userAddress);
        return select;
    }
}
