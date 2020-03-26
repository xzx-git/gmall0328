package com.atguigu.gmall.manage;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallManageWebApplicationTests {

    @Test
    public void contextLoads() /*throws IOException, MyException*/ {
      /*  //配置fdfs的全局信息
        String file = GmallManageWebApplicationTests.class.getClassLoader().getResource("tracker.conf").getFile();
        ClientGlobal.init(file);
        //获得tracker
        TrackerClient tracker = new TrackerClient();
        TrackerServer trackerServer = tracker.getConnection();

        //通过tracker
        StorageClient storageClient = new StorageClient(trackerServer,null);

        //通过storage上传文件
        String[] gifs = storageClient.upload_file("D:/Documents/Pictures/a.jpg","jpg",null);
        String url = "http://192.168.1.10";
        for (String gif : gifs) {
            url = url+"/"+gif;
        }
        System.out.println(url);*/
    }

}
