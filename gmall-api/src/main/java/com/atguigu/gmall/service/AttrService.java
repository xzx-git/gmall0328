package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.bean.BaseAttrValue;

import java.util.List;
import java.util.Set;

public interface AttrService {

    List<BaseAttrInfo> getAttrList(String catalog3Id);

    void savaAttr(BaseAttrInfo baseAttrInfo);

    List<BaseAttrValue> displayAttr(String id);

    void updateAttr(BaseAttrInfo baseAttrInfo);

    void deleteAttr(int id);

    List<BaseAttrInfo> getAttrListByCtg3Id(String spuId);

    List<BaseAttrInfo> getAttrListByValueIds(Set<String> valueIds);
}
