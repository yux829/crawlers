import io.webfolder.cdp.Launcher;
import io.webfolder.cdp.session.Session;
import io.webfolder.cdp.session.SessionFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HttpUtil {

    private final static Logger logger = LoggerFactory.getLogger(HttpUtil.class);
    private final static String url = "https://blog.csdn.net/H_g_y_";
    private static List<String> list = null;

    static {
        list = new ArrayList();
        Document parse = null;
        try {
            parse = Jsoup.parse(new URL(url), 5000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Elements elementsByClass = parse.getElementsByTag("h4");
        for (Element byClass : elementsByClass) {
            String attr = byClass.getElementsByTag("a") == null ? "" : byClass.getElementsByTag("a").attr("href");
            list.add(attr);
        }
        list = list.stream().filter(x -> x.trim().length() > 0).collect(Collectors.toList());
        //先尝试静态网页爬取若爬取不到数据则采动态用爬取网页
        if (!(list.size() > 0)) {

            //注意此方法爬取容内必须安装谷歌浏览器
            ArrayList<String> arguments = new ArrayList<String>();

            //如果添加此行就不会弹出google浏览器:---------注:若阻止浏览器弹出则后台谷歌进程不会关闭,多次请求导致阻塞,返回404
            //arguments.add("--headless");
            Launcher launcher = new Launcher();

            //第一个参数是本地谷歌浏览器的可执行地址
            try (
                    SessionFactory factory = launcher.launch();
                    Session session = factory.create()
            ) {
                //这个参数是你想要爬取的网址
                session.navigate(url);
                //session.wait(100000);
                //等待加载完毕
                session.waitDocumentReady();
                //获得爬取的数据
                String content = (String) session.getContent();
                //使用Jsoup转换成可以解析的Document
                Document document = Jsoup.parse(content);
//                System.out.println(document.html());
                //关闭资源
                session.close();
                factory.close();
                Elements blogs = parse.getElementsByTag("article");
                for (Element byClass : blogs) {
                    String attr = byClass.getElementsByTag("a") == null ? "" : byClass.getElementsByTag("a").attr("href");
                    list.add(attr);
                }
                list = list.stream().filter(x -> x.trim().length() > 0).collect(Collectors.toList());
                list.stream().forEach(logger::trace);
            } catch (Exception e) {
                e.printStackTrace();
            }
            launcher.kill();
        }


    }

    public static void main(String[] args) {
        list.forEach(logger::debug);
    }
}
