package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.BaseSaleAttr;
import com.atguigu.gmall.bean.SpuImage;
import com.atguigu.gmall.bean.SpuInfo;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
public class SpuController {

    @Reference
    SpuService spuService;


    @RequestMapping("getSpuImageListBySpuId")
    @ResponseBody
    public List<SpuImage> getSpuImageListBySpuId(String spuId){
        List<SpuImage> spuImage =spuService.getSpuImageListBySpuId(spuId);
        return spuImage;
    }

    @RequestMapping("getSaleAttrListBySpuId")
    @ResponseBody
    public List<SpuSaleAttr> getSaleAttrListBySpuId(String spuId){
        List<SpuSaleAttr> spuInfos =spuService.getSaleAttrListBySpuId(spuId);
        return spuInfos;
    }


    @ResponseBody
    @RequestMapping("fileUpload")
    public String fileUpload(@RequestParam("file") MultipartFile file){
        return "https://img13.360buyimg.com/da/s590x470_jfs/t1/102159/15/9818/63079/5e12ff72E92567434/4bc01bcb820cc0f0.jpg.webp";
    }

    @ResponseBody
    @RequestMapping("saveSpu")
    public String saveSpu(SpuInfo spuInfo){
        spuService.saveSpu(spuInfo);
        return "success";
    }

    @ResponseBody
    @RequestMapping("baseSaleAttrList")
    public List<BaseSaleAttr> baseSaleAttrList(){
        List<BaseSaleAttr> baseSaleAttrs = spuService.baseSaleAttrList();
        return baseSaleAttrs;
    }

    @ResponseBody
    @RequestMapping("spuList")
    public List<SpuInfo> spuList(String catalog3Id){
        List<SpuInfo> spuInfos = spuService.spuList(catalog3Id);
        return spuInfos;
    }
}
