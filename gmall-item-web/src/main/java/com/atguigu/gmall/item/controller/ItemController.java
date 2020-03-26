package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuSaleAttrValue;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ItemController {

    @Reference
    SkuService skuService;

    @Reference
    SpuService spuService;

    @RequestMapping("{skuId}.html")
    public String item(@PathVariable String skuId, ModelMap map){
        SkuInfo skuInfo = skuService.getSkuById(skuId);
        map.put("skuInfo",skuInfo);
        String spuId = skuInfo.getSpuId();

        //spu的sku和销售属性对应关系的hash表
        List<SkuInfo> infos = spuService.getSkuSaleAttrValueListBySpu(spuId);
        HashMap<String, String> stringStringHashMap = new HashMap<>();
        for (SkuInfo info : infos) {
            String v = info.getId();
            List<SkuSaleAttrValue> skuSaleAttrValueList = info.getSkuSaleAttrValueList();
            String k = "";
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                k = k + "|" + skuSaleAttrValue.getSaleAttrValueId();
            }
            stringStringHashMap.put(k,v);

        }
        String skuJson = JSON.toJSONString(stringStringHashMap);
        map.put("skuJson",skuJson);
        //销售属性列表
        Map<String, String> stringMap = new HashMap<>();
        stringMap.put("spuId",spuId);
        stringMap.put("skuId",skuId);
        List<SpuSaleAttr> saleAttrListBySpuId = spuService.getSpuSaleAttrListCheckBySku(stringMap);
        map.put("spuSaleAttrListCheckBySku",saleAttrListBySpuId);
        return "item";
    }

    @RequestMapping("index")
    public String index(ModelMap map){
        map.put("hello","hello thymeleaf");
        return "dome";
    }
}

