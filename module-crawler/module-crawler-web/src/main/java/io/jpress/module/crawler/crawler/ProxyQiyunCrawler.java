/*
 * Copyright (c) 2018-2019, Eric 黄鑫 (ninemm@126.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jpress.module.crawler.crawler;

import cn.edu.hfut.dmic.webcollector.conf.Configuration;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.jboot.Jboot;
import io.jpress.module.crawler.model.Spider;
import io.jpress.module.crawler.model.util.CrawlerConsts;
import io.jpress.module.crawler.request.ProxyRequester;
import okhttp3.Headers;
import org.joda.time.DateTime;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.Map;

/**
 * 旗云代理
 *
 * http://www.qydaili.com/free/
 * http://www.qydaili.com/free/?action=china&page=2
 *
 * 定时任务：* /5 * * * *
 *
 * @author: Eric Huang
 * @date: 2019-07-03 17:54
 */
public class ProxyQiyunCrawler extends AbstractBreadthCrawler {

    public ProxyQiyunCrawler(String crawlPath, boolean autoParse, Spider spider) {
        super(crawlPath, autoParse, spider);

        Map<String, String> headerMap = Maps.newHashMap();
        headerMap.put("Host", "www.qydaili.com");
        headerMap.put("Referer", "http://www.qydaili.com/free/");
        Headers headers = Headers.of(headerMap);

        // 设置HTTP代理插件
        if (spider.isEnableProxy()) {
            setRequester(new ProxyRequester("http", headers));
        }
    }

    @Override
    protected void parse(Page page, CrawlDatums next) {
        List<String> proxyList = Lists.newArrayList();
        String crawlerTime = DateTime.now().toString("yyyy-MM-dd HH:mm:ss");
        Elements elements = page.select(".container > table > tbody > tr");

        for (Element ele : elements) {
            String ip = ele.child(0).text();
            String port = ele.child(1).text();
            String anonymity = ele.child(2).text();
            String protocl = ele.child(3).text();

            String location = ele.child(4).text();
            String response = ele.child(5).text().replace("秒", "");
            String verifyTime = ele.child(6).text();

            if (StrUtil.isBlank(ip) || StrUtil.isBlank(port) || StrUtil.isBlank(protocl)) {
                continue;
            }

            StringBuilder sqlBuilder = new StringBuilder("insert into proxy_info(`ip`, `port`, `location`, `response`,");
            sqlBuilder.append(" `verify_time`, `protocol`, `anonymity_type`, `crawler_time`, `website`) values(");

            sqlBuilder.append("'").append(ip).append("', ");
            sqlBuilder.append(port).append(", ");
            sqlBuilder.append("'").append(location).append("', ");
            sqlBuilder.append(response).append(", ");

            sqlBuilder.append("'").append(verifyTime).append("', ");
            sqlBuilder.append("'").append(protocl).append("', ");
            sqlBuilder.append("'").append(anonymity).append("', ");

            sqlBuilder.append("'").append(crawlerTime).append("', ");
            sqlBuilder.append("'").append("www.qydaili.com").append("'");
            sqlBuilder.append(")");
            sqlBuilder.append(" on duplicate key update response = " + response);

            // System.out.println(sqlBuilder.append(";").toString());

            proxyList.add(sqlBuilder.toString());
        }
        Jboot.sendEvent(CrawlerConsts.QIYUNPROXY_EVENT_NAME, proxyList);
    }

    @Override
    protected void initConfig() {
        if (spider != null) {
            Configuration conf = Configuration.copyDefault();

            conf.setExecuteInterval(spider.getSleep());
            conf.setMaxExecuteCount(spider.getRetry());
            conf.setConnectTimeout(spider.getTimeout());
            conf.setDefaultUserAgent(spider.getUserAgent());

            this.setConf(conf);
            this.setResumable(true);
            this.setThreads(spider.getThread());
            this.addSeedAndReturn(spider.getStartUrl());

            for (int page = START_PAGE; page <= spider.getMaxPageGather(); page++) {
                this.addSeed(String.format(spider.getStartUrl() + "?action=china&page=%d", page));
            }
        }
    }

    public static void main(String[] args) throws Exception {

        String startUrl = "http://www.qydaili.com/free/";
        ProxyQiyunCrawler crawler = new ProxyQiyunCrawler("qy_crawler", false, null);

        Configuration conf = Configuration.copyDefault();
        conf.setExecuteInterval(5000);
        // conf.setThreadKiller(1);
        // 设置爬取URL数量的上限
        // conf.setTopN(1000);
        conf.setReadTimeout(30000);
        // 连接超时时间
        conf.setConnectTimeout(50000);
        // 最多执行次数
        conf.setMaxExecuteCount(2);
        // conf.setWaitThreadEndTime(5000);

        // conf.setDefaultUserAgent(RandomUtil.randomEle(CrawlerConsts.USER_AGENT));

        crawler.setConf(conf);
        crawler.setThreads(1);
        crawler.addSeedAndReturn(startUrl);

        for (int page = START_PAGE; page <= 10; page++) {
            crawler.addSeed(String.format(startUrl + "?action=china&page=%d", page));
        }
        // crawler.addRegex("https://www.xicidaili.com/wn/*");

        //crawler.setResumable(true);
        // 设置爬取深度
        crawler.start(1);
    }
}
