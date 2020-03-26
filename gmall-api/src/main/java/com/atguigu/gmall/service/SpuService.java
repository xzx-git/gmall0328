package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.*;

import java.util.List;
import java.util.Map;

public interface SpuService {
    List<SpuInfo> spuList(String catalog3Id);

    List<BaseSaleAttr> baseSaleAttrList();

    void saveSpu(SpuInfo spuInfo);

    List<SpuSaleAttr> getSaleAttrListBySpuId(String spuId);

    List<SpuImage> getSpuImageListBySpuId(String spuId);

    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Map<String,String> stringMap);

    List<SkuInfo> getSkuSaleAttrValueListBySpu(String spuId);
}
