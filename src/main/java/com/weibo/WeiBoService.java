package com.weibo;

import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.impl.NutDao;
import org.nutz.http.Header;
import org.nutz.http.Http;
import org.nutz.ioc.Ioc;
import org.nutz.ioc.impl.NutIoc;
import org.nutz.ioc.loader.json.JsonLoader;
import org.nutz.json.Json;

import javax.sql.DataSource;
import java.util.*;

public class WeiBoService {
    private static Dao dao;

    public static void main(String[] args) throws InterruptedException {
        Ioc ioc = new NutIoc(new JsonLoader("./db.js"));
        DataSource dataSource = ioc.get(DataSource.class, "dataSource");
        dao = new NutDao(dataSource);
        // 创建表
        dao.create(Content.class, false); // false的含义是,如果表已经存在,就不要删除重建了.
        //wanhui
        fireAll("2248200494", 215);
        ioc.depose(); // 关闭ioc容器.
    }

    private static int save(Content p) {
        List<Content> c = dao.query(Content.class, Cnd.where("weiboId", "=", p.getWeiboId()));
        if (c != null && c.size() > 0) {
            Content dest = c.get(0);
            dest.setContent(p.getContent());
            dest.setCreatetime(p.getCreatetime());
            dest.setUid(p.getUid());
            return dao.update(dest);
        }
        dao.insert(p);
        return 1;
    }


    private static String get(String url, Map<String, String> params) {
        return Http.get(url, Header.create(params), Integer.MAX_VALUE).getContent();
    }

    private static void fireAll(String uid, int maxSize) throws InterruptedException {
        String url = "https://weibo.com/ajax/statuses/mymblog?feature=0&uid=" + uid + "&page=";
        List<String> errorList=new ArrayList<String>();
        for (int i = 1; i <= maxSize; i++) {
            try {
                fireOne(url + i, uid);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("page " + i + " error !");
                errorList.add(url+i);
            }
            Thread.sleep(2 * 1000);
        }
        for(String e:errorList) {
            fireOne(e,uid);
        }
    }


    private static void fireOne(String url, String uid) {
        System.out.println("start fireOne " + url);
        Map<String, String> params = new HashMap<String, String>();
        params.put("Cookie", "UOR=login.sina.com.cn,weibo.com,login.sina.com.cn; SINAGLOBAL=1223014468202.4946.1601953098522; XSRF-TOKEN=i3Y0JOw729yM5A9co3HyminU; login_sid_t=47b83e7fb5c12df18cd934edbbc32c18; cross_origin_proto=SSL; WBStorage=8daec78e6a891122|undefined; _s_tentry=-; wb_view_log=1920*10801.25; Apache=1197190019979.8347.1623661593066; ULV=1623661593094:6:1:1:1197190019979.8347.1623661593066:1622376558064; ALF=1655197468; SSOLoginState=1623661469; SCF=Aseq5PtqV2Jmch8C8UZ9t3Txs2nVj29nwqt89gTp0OnUC_g2mADxCW-rhk7zHS-v5YFKhRrGBIXYsH-ghBQ7S0g.; SUB=_2A25Nw2vNDeRhGedK61MQ-CvLyjiIHXVuudoFrDV8PUNbmtAKLVH3kW9NJZvmbExgkaWHiB7eIrh9huc-_FakI76b; SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9WWpoD27gYMYGvXSWS0ju5.r5JpX5KzhUgL.Fo2Xeh2p1h-NeKB2dJLoI7xRMg8E; WBPSESS=M2Zta0envgB1i7K9ias77aJN7cgU5qYLPCWrtZAW1CHI5W6z-bBcW_hKKIsIZI5JN5QIyeV9NHcpHaCFQQ0ln5eDB1fbEXEBycjix6MmyT-FVcTH1VCedxXc9fTJ0TnQ");
        String res = get(url, params);
        Object obj = Json.fromJson(res);
        List<Content> plist = new ArrayList<Content>();
        if (obj instanceof Map) {
            Map data = (Map) ((Map) obj).get("data");
            List<Map> list = (List<Map>) data.get("list");
            for (Map map : list) {
                Content p = new Content();
                for (Object key : map.keySet()) {
                    if ("created_at".equalsIgnoreCase("" + key)) {
                        p.setCreatetime(new Date("" + map.get(key)));
                    } else if ("text".equalsIgnoreCase("" + key)) {
                        String text = ("" + map.get(key)).replaceAll("<br />", "").replaceAll("<span class=\"expand\">展开</span>", "");
//                        System.out.println(text);
                        text = text.replaceAll("<img[^>]*/>", " ");
                        text = text.replaceAll("<a href[^>]*>", " ");
                        text = text.replaceAll("</a>", " ");
                        text = removeSpeChar(text);
//                        System.out.println(text);
                        p.setContent(text);
                    } else if ("mid".equalsIgnoreCase("" + key)) {
                        p.setWeiboId("" + map.get(key));
                    }
                }
                p.setUid(uid);
                plist.add(p);
            }
        }
        for (Content c : plist) {
            save(c);
        }
        System.out.println("end fireOne " + url);
    }

    private static String removeSpeChar(String str) {
        char[] chars = str.toCharArray();
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < chars.length; i++) {
            if ((chars[i] >= 19968 && chars[i] <= 40869) || (chars[i] >= 97 && chars[i] <= 122) || (chars[i] >= 65 && chars[i] <= 90)) {
                buffer.append(chars[i]);
            }
        }
        return buffer.toString();
    }

    private static String sqlFilter(String str) {
        str = str.replaceAll("\\.", "。");
        str = str.replaceAll(":", "：");
        str = str.replaceAll(";", "；");
        str = str.replaceAll("&", "＆");
        str = str.replaceAll("<", "＜");
        str = str.replaceAll(">", "＞");
        str = str.replaceAll("'", "＇");
        str = str.replaceAll("\"", "“");
        str = str.replaceAll("--", "－－");
        str = str.replaceAll("/", "／");
        str = str.replaceAll("%", "％");
        str = str.replaceAll("\\+", "＋");
        str = str.replaceAll("\\(", "（");
        str = str.replaceAll("\\)", "）");
        return str;
    }
}
