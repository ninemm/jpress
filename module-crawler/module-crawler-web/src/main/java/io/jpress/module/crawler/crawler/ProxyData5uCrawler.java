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
import io.jpress.commons.utils.DateUtils;
import io.jpress.module.crawler.model.Spider;
import io.jpress.module.crawler.model.util.CrawlerConsts;
import io.jpress.module.crawler.request.ProxyRequester;
import okhttp3.Headers;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.Map;

/**
 * 无忧代理
 *
 * http://www.data5u.com/
 *
 * 定时任务：* /3 * * * *
 *
 * @author: Eric Huang
 * @date: 2019-07-03 17:54
 */
public class ProxyData5uCrawler extends AbstractBreadthCrawler {

    public ProxyData5uCrawler(String crawlPath, boolean autoParse, Spider spider) {
        super(crawlPath, autoParse, spider);

        Map<String, String> headerMap = Maps.newHashMap();
        headerMap.put("Host", "www.data5u.com");
        headerMap.put("Referer", "http://www.data5u.com");
        Headers headers = Headers.of(headerMap);

        // 设置HTTP代理插件
        if (spider != null && spider.isEnableProxy()) {
            setRequester(new ProxyRequester("http", headers));
        }
    }

    @Override
    protected void parse(Page page, CrawlDatums next) {
        List<String> proxyList = Lists.newArrayList();
        String crawlerTime = LocalDateTime.now().toString(DateUtils.DEFAULT_FORMATTER);
        Elements elements = page.select(".wlist > ul > li:nth-child(2) > ul.l2");

        for (Element ele : elements) {
            String ip = ele.child(0).text();
            String port = ele.child(1).text();
            String anonymity = ele.child(2).text();
            String protocol = ele.child(3).text();

            String location = ele.child(5).text();
            String isp = ele.child(6).text();
            String response = ele.child(7).text().trim().replace("秒", "");

            if (StrUtil.isBlank(ip) || StrUtil.isBlank(port) || StrUtil.isBlank(protocol)) {
                continue;
            }

            StringBuilder sqlBuilder = new StringBuilder("insert into proxy_info(`ip`, `port`, `location`, `response`,");
            sqlBuilder.append(" `protocol`, `anonymity_type`, `crawler_time`, `website`) values(");

            sqlBuilder.append("'").append(ip).append("', ");
            sqlBuilder.append(port).append(", ");
            sqlBuilder.append("'").append(location).append("', ");
            sqlBuilder.append(response).append(", ");

            sqlBuilder.append("'").append(protocol).append("', ");
            sqlBuilder.append("'").append(anonymity).append("', ");
            sqlBuilder.append("'").append(crawlerTime).append("', ");
            sqlBuilder.append("'").append("www.data5u.com").append("'");

            sqlBuilder.append(")");
            sqlBuilder.append(" on duplicate key update response = " + response);
            proxyList.add(sqlBuilder.toString());
            // System.out.println(sqlBuilder.append(";").toString());
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
            if (spider.isResumable()) {
                this.setResumable(true);
            }
            this.setThreads(spider.getThread());
            this.addSeedAndReturn(spider.getStartUrl());
        }
    }

    public static void main(String[] args) throws Exception {

        String startUrl = "http://www.data5u.com/";
        ProxyData5uCrawler crawler = new ProxyData5uCrawler("crawler/data5u", false, null);

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

        conf.setDefaultUserAgent(RandomUtil.randomEle(CrawlerConsts.USER_AGENT));

        crawler.setConf(conf);
        crawler.setThreads(1);
        crawler.addSeedAndReturn(startUrl);

        // crawler.addRegex("https://www.xicidaili.com/wn/*");

        //crawler.setResumable(true);
        // 设置爬取深度
        crawler.start(1);
    }
}
