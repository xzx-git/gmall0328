package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.SkuAttrValue;
import com.atguigu.gmall.bean.SkuImage;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuSaleAttrValue;
import com.atguigu.gmall.manage.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.manage.mapper.SkuImageMapper;
import com.atguigu.gmall.manage.mapper.SkuInfoMapper;
import com.atguigu.gmall.manage.mapper.SkuSaleAttrValueMapper;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.util.List;
@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    SkuInfoMapper skuInfoMapper;

    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    SkuImageMapper skuImageMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public List<SkuInfo> getSkuListBySpu(String spuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setSpuId(spuId);
        List<SkuInfo> select = skuInfoMapper.select(skuInfo);
        return select;
    }

    @Override
    public void saveSku(SkuInfo skuInfo) {
        skuInfoMapper.insertSelective(skuInfo);
        String skuId = skuInfo.getId();
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        for (SkuAttrValue skuAttrValue : skuAttrValueList) {
            skuAttrValue.setSkuId(skuId);
            skuAttrValueMapper.insert(skuAttrValue);

        }
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
            skuSaleAttrValue.setSkuId(skuId);
            skuSaleAttrValueMapper.insert(skuSaleAttrValue);
        }
       /* List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        for (SkuImage skuImage : skuImageList) {
            skuImage.setSkuId(skuId);
            skuImageMapper.insert(skuImage);
        }*/
    }

    @Override
    public SkuInfo getSkuById(String skuId) {
        Jedis jedis = null;
        try{
            jedis = redisUtil.getJedis();
        }catch (Exception e){
            return null;
        }

        SkuInfo skuInfo = null;
        //查询缓存
        String key = "sku:"+skuId+":info";
        String val = jedis.get(key);
        if ("empty".equals(val)){
            System.out.println(Thread.currentThread().getName() + "发现数据库中暂时还没有该商品，直接返回空对象");
            return skuInfo;
        }


        if (StringUtils.isBlank(val)){
            System.out.println(Thread.currentThread().getName() + "发现缓存中没有数据，申请分布式锁");

            //申请缓存锁
            String OK = jedis.set("sku:" + skuId + ":lock", "1", "nx", "px", 5000);

            if ("OK".equals(OK)){//拿到缓存锁

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getName()+"获得分布式锁，开始访问数据");
                //查询db
                skuInfo = getSkuByIdFormDb(skuId);
                if (skuInfo!=null){
                    System.out.println(Thread.currentThread().getName()+"通过分布式锁，查询到数据，同步缓存，然后还锁");

                    //同步缓存
                    jedis.set(key,JSON.toJSONString(skuInfo));
                }else{
                    System.out.println(Thread.currentThread().getName()+"通过分布式锁，没有查询到数据，通知同伴在10秒之内不要访问该sku");
                    //通知同伴
                    jedis.setex("sku"+skuId+":info",10,"empty");
                }
                //归还缓存锁
                System.out.println(Thread.currentThread().getName()+"归还分布式锁");
                jedis.del("sku"+skuId+":lock");

            }else{//没有拿到缓存锁
                System.out.println(Thread.currentThread().getName()+"发现分布式锁被占用，开始自旋");
                //自旋
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                getSkuById(skuId);

            }
        }else{
            //正常转换缓存数据
            System.out.println(Thread.currentThread().getName()+"正常从缓存中取得数据，返回结果");
            skuInfo = JSON.parseObject(val, SkuInfo.class);
        }

        return skuInfo;
    }

    @Override
    public List<SkuInfo> getSkuListByCatalog3Id(String catalog3Id) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setCatalog3Id(catalog3Id);
        List<SkuInfo> select = skuInfoMapper.select(skuInfo);
        for (SkuInfo info : select) {
            String id = info.getId();
            SkuAttrValue skuAttrValue = new SkuAttrValue();
            skuAttrValue.setSkuId(id);
            List<SkuAttrValue> select1 = skuAttrValueMapper.select(skuAttrValue);
            info.setSkuAttrValueList(select1);
        }
        return select;
    }

    @Override
    public boolean checkPrice(BigDecimal skuPrice, String skuId) {
        boolean b = false;
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        SkuInfo skuInfo1 = skuInfoMapper.selectOne(skuInfo);
        if (skuInfo1!=null){
            int i = skuInfo1.getPrice().compareTo(skuPrice);
            if (i==0){
                b = true;
            }
        }
        return b;
    }

    private SkuInfo getSkuByIdFormDb(String skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        SkuInfo skuInfo1 = skuInfoMapper.selectOne(skuInfo);

        if (skuInfo1 != null){
            SkuImage skuImage = new SkuImage();
            skuImage.setSkuId(skuId);
            List<SkuImage> select = skuImageMapper.select(skuImage);
            skuInfo1.setSkuImageList(select);
            SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
            skuSaleAttrValue.setSkuId(skuId);
            List<SkuSaleAttrValue> select1 = skuSaleAttrValueMapper.select(skuSaleAttrValue);
            skuInfo1.setSkuSaleAttrValueList(select1);
        }

        return skuInfo1;
    }

}
