package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.bean.BaseAttrValue;
import com.atguigu.gmall.manage.mapper.BaseAttrInfoMapper;
import com.atguigu.gmall.manage.mapper.BaseAttrValueMapper;
import com.atguigu.gmall.service.AttrService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

@Service
public class AttrServiceImpl implements AttrService {

    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;

    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
        baseAttrInfo.setCatalog3Id(catalog3Id);
        List<BaseAttrInfo> select = baseAttrInfoMapper.select(baseAttrInfo);
        return select;
    }

    @Override
    public void savaAttr(BaseAttrInfo baseAttrInfo) {
         baseAttrInfoMapper.insertSelective(baseAttrInfo);
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();

        for (BaseAttrValue baseAttrValue : attrValueList) {
            baseAttrValue.setAttrId(baseAttrInfo.getId());
            baseAttrValueMapper.insert(baseAttrValue);
        }
    }

    @Override
    public List<BaseAttrValue> displayAttr(String id) {
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(id);
        List<BaseAttrValue> select = baseAttrValueMapper.select(baseAttrValue);
        return select;
    }

    @Override
    public void updateAttr(BaseAttrInfo baseAttrInfo) {
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();

        for (BaseAttrValue baseAttrValue : attrValueList) {
            baseAttrValueMapper.updateByPrimaryKey(baseAttrValue);
        }
    }

    @Override
    public void deleteAttr(int id) {
        baseAttrValueMapper.deleteAttr(id);
        String idd=Integer.toString(id);
        baseAttrInfoMapper.deleteByPrimaryKey(idd);
    }

    @Override
    public List<BaseAttrInfo> getAttrListByCtg3Id(String catalog3Id) {
        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
        baseAttrInfo.setCatalog3Id(catalog3Id);
        List<BaseAttrInfo> select = baseAttrInfoMapper.select(baseAttrInfo);
        for (BaseAttrInfo attrInfo : select) {
            String attrId = attrInfo.getId();
            BaseAttrValue baseAttrValue = new BaseAttrValue();
            baseAttrValue.setAttrId(attrId);
            List<BaseAttrValue> select1 = baseAttrValueMapper.select(baseAttrValue);
            attrInfo.setAttrValueList(select1);
        }

        return select;
    }

    @Override
    public List<BaseAttrInfo> getAttrListByValueIds(Set<String> valueIds) {
        String join = StringUtils.join(valueIds,",");
        List<BaseAttrInfo> baseAttrInfos = baseAttrValueMapper.selectAttrListByValueIds(join);
        return baseAttrInfos;
    }
}
