package io.jpress.module.crawler.crawler;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;
import com.jfinal.aop.Aop;
import com.jfinal.log.Log;
import io.jpress.module.crawler.model.Spider;
import io.jpress.module.crawler.service.SpiderService;

/**
 * 百度长尾词、关键词采集
 *
 * @author Eric.Huang
 * @date 2019-05-29 23:30
 * @package io.jpress.module.crawler.crawler
 **/

public class BaiduKeywordCrawler extends BreadthCrawler {

    private static final Log _LOG = Log.getLog(BaiduKeywordCrawler.class);

    private Object spiderId;

    public BaiduKeywordCrawler(String crawlPath, boolean autoParse) {
        super(crawlPath, autoParse);
    }

    @Override
    public void visit(Page page, CrawlDatums crawlDatums) {

    }

    public void start() {

        Spider spider = Aop.get(SpiderService.class).findById(spiderId);


    }
}
