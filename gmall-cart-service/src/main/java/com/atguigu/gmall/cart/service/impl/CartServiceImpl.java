package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    CartInfoMapper cartInfoMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public CartInfo ifCartExists(CartInfo cartInfo) {
        CartInfo cartInfo1 = new CartInfo();
        cartInfo1.setUserId(cartInfo.getUserId());
        cartInfo1.setSkuId(cartInfo.getSkuId());
        CartInfo select = cartInfoMapper.selectOne(cartInfo1);
        return select;
    }

    @Override
    public void updateCart(CartInfo cartInfoDb) {
        cartInfoMapper.updateByPrimaryKeySelective(cartInfoDb);
    }

    @Override
    public void saveCart(CartInfo cartInfo) {
        cartInfoMapper.insertSelective(cartInfo);
    }

    @Override
    public void syncCache(String userId) {

        Jedis jedis = redisUtil.getJedis();

        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        List<CartInfo> select = cartInfoMapper.select(cartInfo);

        if (select == null || select.size()==0){
            jedis.del("carts:"+userId+":info");
        }else {
            HashMap<String, String> stringStringHashMap = new HashMap<>();
            for (CartInfo info : select) {
                stringStringHashMap.put(info.getId(), JSON.toJSONString(info));
            }
            jedis.hmset("carts:"+userId+":info",stringStringHashMap);
            jedis.close();
        }
    }

    @Override
    public List<CartInfo> getCartCache(String userId) {

        List<CartInfo> cartInfos = new ArrayList<>();
        Jedis jedis = redisUtil.getJedis();
        List<String> hvals = jedis.hvals("carts:" + userId + ":info");

        if (hvals != null && hvals.size()>0){
            for (String hval : hvals) {
                CartInfo cartInfo = JSON.parseObject(hval, CartInfo.class);
                cartInfos.add(cartInfo);
            }
        }
        return cartInfos;
    }

    @Override
    public void updateCartChecked(CartInfo cartInfo) {
        Example e = new Example(CartInfo.class);
        e.createCriteria().andEqualTo("skuId",cartInfo.getSkuId()).andEqualTo("userId",cartInfo.getUserId());
        cartInfoMapper.updateByExampleSelective(cartInfo,e);
        syncCache(cartInfo.getUserId());
    }

    @Override
    public void combineCart(List<CartInfo> cartInfos, String userId) {
            if (cartInfos!=null){
                for (CartInfo cartInfo : cartInfos) {
                    CartInfo info = ifCartExists(cartInfo);
                    if (info == null){
                        //插入
                        cartInfo.setUserId(userId);
                        cartInfoMapper.insertSelective(cartInfo);
                    }else {
                        //更新
                        info.setSkuNum(cartInfo.getSkuNum()+info.getSkuNum());
                        info.setCartPrice(info.getSkuPrice().multiply(new BigDecimal(info.getSkuNum())));
                        cartInfoMapper.updateByPrimaryKeySelective(info);
                    }
                }
            }
            //同步缓存
        syncCache(userId);
    }

    @Override
    public List<CartInfo> getCartCacheByChecked(String userId) {

        List<CartInfo> cartInfos = new ArrayList<>();
        Jedis jedis = redisUtil.getJedis();
        List<String> hvals = jedis.hvals("carts:" + userId + ":info");

        if (hvals != null && hvals.size()>0){
            for (String hval : hvals) {
                CartInfo cartInfo = JSON.parseObject(hval, CartInfo.class);
                if (cartInfo.getIsChecked().equals("1")){
                    cartInfos.add(cartInfo);
                }
            }
        }
        return cartInfos;
    }

    @Override
    public void deleteCartById(List<CartInfo> cartInfos) {
        //delete from cart_info where id in ()
        for (CartInfo cartInfo : cartInfos) {
            cartInfoMapper.deleteByPrimaryKey(cartInfo);
        }
        //同步缓存
        syncCache(cartInfos.get(0).getUserId());
    }
}
