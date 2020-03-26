package com.atguigu.gmall.manage.mapper;

import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.bean.BaseAttrValue;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrValueMapper extends Mapper<BaseAttrValue> {

    int deleteAttr(int attrInfoId);

    List<BaseAttrInfo> selectAttrListByValueIds(@Param("ids") String join);
}
