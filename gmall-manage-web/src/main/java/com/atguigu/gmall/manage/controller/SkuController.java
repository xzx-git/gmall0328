package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class SkuController {

    @Reference
    SkuService skuService;

    @RequestMapping("saveSku")
    @ResponseBody
    public String saveSku(SkuInfo skuInfo){
        skuService.saveSku(skuInfo);
        return "success";
    }
    @RequestMapping("getSkuListBySpu")
    @ResponseBody
    public List<SkuInfo> getSkuListBySpu(String spuId){
        List<SkuInfo> skuInfo = skuService.getSkuListBySpu(spuId);
        return skuInfo;
    }
}
