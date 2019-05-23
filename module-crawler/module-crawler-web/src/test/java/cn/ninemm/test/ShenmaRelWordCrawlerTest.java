package cn.ninemm.test;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatum;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.ram.RamCrawler;
import cn.edu.hfut.dmic.webcollector.util.ExceptionUtils;
import cn.hutool.core.util.CharsetUtil;
import io.jboot.components.http.JbootHttpManager;
import io.jboot.components.http.JbootHttpRequest;
import io.jboot.utils.StrUtil;
import io.jpress.commons.utils.JsoupUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * 神马相关关键词测试
 *
 * @author Eric.Huang
 * @date 2019-05-22 13:02
 * @package cn.ninemm.test
 **/

public class ShenmaRelWordCrawlerTest extends RamCrawler {

    public ShenmaRelWordCrawlerTest(String keyword, int pageNum) throws Exception {
        // for (int pageIndex = 1; pageIndex <= pageNum; pageIndex++) {
            String url = createBingUrl(keyword, pageNum);
            CrawlDatum datum = new CrawlDatum(url)
                    .type("searchEngine")
                    .meta("keyword", keyword)
                    .meta("pageIndex", pageNum)
                    .meta("depth", 1);
            addSeed(datum);
        //}
    }


    @Override
    public void visit(Page page, CrawlDatums next) {
        if (page.code() == 301 || page.code() == 302) {
            try {
                String redirectUrl = new URL(new URL(page.url()), page.location()).toExternalForm();
                next.addAndReturn(redirectUrl).meta(page.copyMeta());
            } catch (MalformedURLException e) {
                //the way to handle exceptions in WebCollector
                ExceptionUtils.fail(e);
            }
            return;
        }
        System.out.println(page.html());

        Elements results = page.select("#sider>.sider-card>ul>li>.news-title");
        if (results.size() == 0) {
            results = page.select(".ali_rel>ul>li>a");
        }

        // _LOG.info("关键词：" + keyword + ", 在神马中的相关(搜索)关键词个数：" + results.size());

        if (results.size() > 0) {
            for (int rank = 0; rank < results.size(); rank++) {
                Element result = results.get(rank);
                System.out.println(result.text());
            }
        }
    }

    public static void main(String[] args) throws Exception {


        ShenmaRelWordCrawlerTest crawler = new ShenmaRelWordCrawlerTest("网络爬虫", 1);
        // crawler.start();

        String url = String.format("https://m.sm.cn/s?q=%s&from=smor&safe=1", "文章采集");
        String userAgent = "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.157 Mobile Safari/537.36";
        JbootHttpRequest request = JbootHttpRequest.create(url);
        request.addHeader("User_Agent", userAgent);
        String content = JbootHttpManager.me().getJbootHttp().handle(request).getContent();
        System.out.println(content);
        Elements es = Jsoup.parseBodyFragment(content).select(".ali_rel>ul>li>a");
        if (es != null && es.size() > 0) {
            for (int rank = 0; rank < es.size(); rank++) {
                Element result = es.get(rank);
                System.out.println(result.text());
            }
        }

    }

    /**
     * construct the Bing Search url by the search keyword and the pageIndex
     * @param keyword
     * @param pageIndex
     * @return the constructed url
     * @throws Exception
     */
    public static String createBingUrl(String keyword, int pageIndex) throws Exception {
        try {
            keyword = URLEncoder.encode(keyword, CharsetUtil.UTF_8);
            return String.format("https://m.sm.cn/s?q=%s&from=smor&safe=1", keyword);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
