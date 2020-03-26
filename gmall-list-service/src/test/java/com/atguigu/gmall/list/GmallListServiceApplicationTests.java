package com.atguigu.gmall.list;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.service.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.beanutils.BeanUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallListServiceApplicationTests {

    @Autowired
    JestClient jestClient;

    @Reference
    SkuService skuService;

    public static String getMyDsl(){
        //创建一个dsl工具对象
        SearchSourceBuilder dsl = new SearchSourceBuilder();
        //创建一个先过滤后搜索的query对象
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        TermQueryBuilder t1 = new TermQueryBuilder("catalog3Id", "1260");
        boolQueryBuilder.filter(t1);
        TermQueryBuilder t2 = new TermQueryBuilder("skuAttrValueList.valueId", "13");
        boolQueryBuilder.filter(t2);

        //搜索语句
        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", "小米");
        boolQueryBuilder.must(matchQueryBuilder);
        MatchQueryBuilder matchQueryBuilder1 = new MatchQueryBuilder("skuDesc", "小米");
        boolQueryBuilder.must(matchQueryBuilder1);

        //将query和form和size放入dsl
        dsl.query(boolQueryBuilder);
        dsl.size(10);
        dsl.from(0);

        return dsl.toString();
    }


    @Test
    public void search() {
        List<SkuLsInfo> skuLsInfos = new ArrayList<>();
        Search search = new Search.Builder(getMyDsl()).addIndex("gmall0328").addType("SkuLsInfo").build();
        try {
            SearchResult execute = jestClient.execute(search);
            List<SearchResult.Hit<SkuLsInfo, Void>> hits = execute.getHits(SkuLsInfo.class);
            for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
                SkuLsInfo source =  hit.source;
                skuLsInfos.add(source);
            }
            System.out.println(skuLsInfos.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void contextLoads() {
        /*//查询mysql中的sku信息
        List<SkuInfo> skuInfoList = skuService.getSkuListByCatalog3Id("1260");
        //转化es中的sku信息
        List<SkuLsInfo> skuLsInfos = new ArrayList<>();
        for (SkuInfo skuInfo : skuInfoList) {

            SkuLsInfo skuLsInfo = new SkuLsInfo();
            try {
                BeanUtils.copyProperties(skuLsInfo,skuInfo);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            skuLsInfos.add(skuLsInfo);
        }
        //导入到es中
        for (SkuLsInfo skuLsInfo : skuLsInfos) {
            Index build = new Index.Builder(skuLsInfo).index("gmall0328").type("SkuLsInfo").id(skuLsInfo.getId()).build();
            System.out.println(build.toString());
            try {
                jestClient.execute(build);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
        // 查询mysql中的sku信息
        List<SkuInfo> skuInfoList = skuService.getSkuListByCatalog3Id("1260");

        // 转化es中的sku信息
        List<SkuLsInfo> skuLsInfos = new ArrayList<>();

        for (SkuInfo skuInfo : skuInfoList) {
            SkuLsInfo skuLsInfo = new SkuLsInfo();

            try {
                BeanUtils.copyProperties(skuLsInfo,skuInfo);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

            skuLsInfos.add(skuLsInfo);
        }

        // 导入到es中
        for (SkuLsInfo skuLsInfo : skuLsInfos) {
            String id = skuLsInfo.getId();
            //long skuId = Long.parseLong(id);
            Index build = new Index.Builder(skuLsInfo).index("gmall0328").type("SkuLsInfo").id(id).build();

            System.out.println(build.toString());
            try {
                jestClient.execute(build);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
