package com.weibo;


import org.junit.Test;
import org.nutz.http.Header;
import org.nutz.http.Http;
import org.nutz.json.Json;


import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;

public class WeiBoUtil {
    //万卉 215
//    private final  static  String URL="https://weibo.com/ajax/statuses/mymblog?uid=2248200494&page=2&feature=0";


    public static String get(String url, Map<String, String> params) {
        return Http.get(url, Header.create(params), Integer.MAX_VALUE).getContent();
    }

    @Test
    public void testWanHui() {
        try {
            caiji("wanhui20210615-2.txt", "2248200494", 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void caijipage(String url, Path file) throws IOException {
        System.out.println("start caiji " + url);
        Map<String, String> params = new HashMap<String, String>();
        params.put("Cookie", "UOR=login.sina.com.cn,weibo.com,login.sina.com.cn; SINAGLOBAL=1223014468202.4946.1601953098522; XSRF-TOKEN=i3Y0JOw729yM5A9co3HyminU; login_sid_t=47b83e7fb5c12df18cd934edbbc32c18; cross_origin_proto=SSL; WBStorage=8daec78e6a891122|undefined; _s_tentry=-; wb_view_log=1920*10801.25; Apache=1197190019979.8347.1623661593066; ULV=1623661593094:6:1:1:1197190019979.8347.1623661593066:1622376558064; ALF=1655197468; SSOLoginState=1623661469; SCF=Aseq5PtqV2Jmch8C8UZ9t3Txs2nVj29nwqt89gTp0OnUC_g2mADxCW-rhk7zHS-v5YFKhRrGBIXYsH-ghBQ7S0g.; SUB=_2A25Nw2vNDeRhGedK61MQ-CvLyjiIHXVuudoFrDV8PUNbmtAKLVH3kW9NJZvmbExgkaWHiB7eIrh9huc-_FakI76b; SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9WWpoD27gYMYGvXSWS0ju5.r5JpX5KzhUgL.Fo2Xeh2p1h-NeKB2dJLoI7xRMg8E; WBPSESS=M2Zta0envgB1i7K9ias77aJN7cgU5qYLPCWrtZAW1CHI5W6z-bBcW_hKKIsIZI5JN5QIyeV9NHcpHaCFQQ0ln5eDB1fbEXEBycjix6MmyT-FVcTH1VCedxXc9fTJ0TnQ");
        String res = get(url, params);
        Object obj = Json.fromJson(res);
        List<String> content = new ArrayList<String>();
        if (obj instanceof Map) {
            Map data = (Map) ((Map) obj).get("data");
            List<Map> list = (List<Map>) data.get("list");
            for (Map map : list) {
                StringBuffer sb = new StringBuffer();
                for (Object key : map.keySet()) {
                    if ("created_at".equalsIgnoreCase("" + key)) {
                        sb.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date("" + map.get(key)))).append("\n");
                    } else if ("text".equalsIgnoreCase("" + key)) {
                        sb.append(map.get(key)).append("\n\n");
                    }
//                    else if ("mid".equalsIgnoreCase("" + key)) {
//                        sb.append(map.get(key)).append("");
//                    }
                }
                content.add(sb.toString().replaceAll("<br />", "").replaceAll("<span class=\"expand\">展开</span>", ""));
            }
        }
        for (int i = content.size() - 1; i >= 0; i--) {
            Files.write(file, (content.get(i) + "\n").getBytes(), StandardOpenOption.APPEND);
        }
        System.out.println("end caiji " + url);
    }


    private static void caiji(String fileName, String uid, int maxSize) throws IOException, InterruptedException {
        String url = "https://weibo.com/ajax/statuses/mymblog?feature=0&uid=" + uid + "&page=";
        Files.deleteIfExists(Paths.get(fileName));
        Path file = Files.createFile(Paths.get(fileName));
        for (int i = maxSize; i >= 1; i--) {
            String getURL = url + i;
            try {
                caijipage(getURL, file);
                Thread.sleep(2 * 1000);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("error try again  " + getURL);
                caijipage(getURL, file);
                Thread.sleep(2 * 1000);
            }
        }
    }


    @Test
    public void Test2() {
        Date time = new Date("Tue Jun 08 14:03:50 +0800 2021");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeFormat = sdf.format(time);
        System.out.println(timeFormat);
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

}
