package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.bean.BaseAttrValue;
import com.atguigu.gmall.bean.BaseCatalog3;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.service.AttrService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class AttrController {

    @Reference
    AttrService attrService;

    @RequestMapping("getAttrListByCtg3Id")
    @ResponseBody
    public List<BaseAttrInfo> getAttrListByCtg3Id(String spuId){
        List<BaseAttrInfo> baseAttrInfo = attrService.getAttrListByCtg3Id(spuId);
        return baseAttrInfo;
    }


    @ResponseBody
    @RequestMapping("getAttrList")
    public List<BaseAttrInfo> getAttrList(String catalog3Id){
        List<BaseAttrInfo> baseAttrInfos = attrService.getAttrList(catalog3Id);
        return baseAttrInfos;
    }



    @ResponseBody
    @RequestMapping("saveAttr")
    public String saveAttr(BaseAttrInfo baseAttrInfo){

        attrService.savaAttr(baseAttrInfo);
        return "success";
    }


    @ResponseBody
    @RequestMapping("displayAttr")
    public List<BaseAttrValue> displayAttr(String id){
        List<BaseAttrValue> baseAttrValues = attrService.displayAttr(id);
        return baseAttrValues;
    }

    @ResponseBody
    @RequestMapping("updateAttr")
    public String updateAttr(BaseAttrInfo baseAttrInfo){
        attrService.updateAttr(baseAttrInfo);
        return "success";
    }


    @ResponseBody
    @RequestMapping("deleteAttr")
    public String deleteAttr(int id){
        attrService.deleteAttr(id);
        return "success";
    }
}
