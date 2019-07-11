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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jfinal.log.Log;
import io.jpress.module.crawler.enums.ProxySite;
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
 * 西刺代理抓取
 *
 * http://www.xicidaili.com/nt/ <BR/>
 * http://www.xicidaili.com/nt/2 <BR/>
 * http://www.xicidaili.com/nn/ <BR/>
 *
 * 已经停用
 * http://www.xicidaili.com/wn/ <BR/>
 * http://www.xicidaili.com/wt/ <BR/>
 *
 * @author: Eric Huang
 * @date: 2019/6/21 16:31
 */
public class ProxyXiciCrawler extends AbstractBreadthCrawler {

    private static final Log _LOG = Log.getLog(ProxyXiciCrawler.class);

    private static final String URL = "https://www.xicidaili.com/nn/";

    private static final Map<String, String> REGION_MAP = Maps.newHashMap();
    static {
        REGION_MAP.put("nn", "国内高匿");
        REGION_MAP.put("nt", "国内普通");
        REGION_MAP.put("wn", "国内HTTPS");
        REGION_MAP.put("wt", "国内HTTP");
    }

    public ProxyXiciCrawler(String crawlPath, boolean autoParse, Spider spider) {
        super(crawlPath, autoParse, spider);

        Map<String, String> headerMap = Maps.newHashMap();
        headerMap.put("Host", "www.xicidaili.com");
        headerMap.put("Referer", "https://www.xicidaili.com/wn/");
        headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36");
        headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
        headerMap.put("Accept-Encoding", "gzip, deflate, br");

        headerMap.put("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
        headerMap.put("Connection", "keep-alive");
        headerMap.put("Upgrade-Insecure-Requests", "1");
        headerMap.put("Cookie", "_free_proxy_session=BAh7B0kiD3Nlc3Npb25faWQGOgZFVEkiJTk3NzJmNDY0YjRiODJkMjJjMDhkNThhMDg5Y2IzMmRjBjsAVEkiEF9jc3JmX3Rva2VuBjsARkkiMXRYMUoyOHBSaGt1dy9iYWpBWVp2OW5RcWt2YWJnbExieUJGMUU5MzFSbFk9BjsARg==--a3e3c1444a27548f564d23b9bf877c29a0a04da7");
        Headers headers = Headers.of(headerMap);

        // 设置HTTP代理插件
        if (spider != null && spider.isEnableProxy()) {
            setRequester(new ProxyRequester("https", headers));
        }

        this.addSeedAndReturn(URL);

        for (int page = START_PAGE; page <= 3; page++) {
            this.addSeed(String.format(URL + "%d", page));
        }
    }

    @Override
    public void parse(Page page, CrawlDatums crawlDatums) {

        List<String> sqlList = Lists.newArrayList();
        String crawlerTime = DateTime.now().toString("yyyy-MM-dd HH:mm:ss");
        Elements elements = page.select("#ip_list > tbody > tr:gt(0)");

        for (Element ele : elements) {

            String ip = ele.child(1).text();
            String port = ele.child(2).text();
            String location = ele.child(3).tagName("a").text();
            String anonymity = ele.child(4).text();

            String protocl = ele.child(5).text();
            String response = ele.child(6).child(0).tagName("div").attr("title").replace("秒", "");
            /*String connectTime = ele.child(7).child(0).tagName("div").attr("title").replace("秒", "");
            String existTime = ele.child(8).text();*/
            String verifyTime = ele.child(9).text();

            StringBuilder sqlBuilder = new StringBuilder("insert into proxy_info(`ip`, `port`, `location`, `response`,");
            sqlBuilder.append(" `verify_time`, `protocol`, `anonymity_type`, `crawler_time`, `website`) values(");

            sqlBuilder.append("'").append(ip).append("', ");
            sqlBuilder.append(port).append(", ");
            sqlBuilder.append("'").append(location).append("', ");
            sqlBuilder.append(response).append(", ");

            sqlBuilder.append("'").append("20" + verifyTime + ":00").append("', ");
            sqlBuilder.append("'").append(protocl).append("', ");
            sqlBuilder.append("'").append(anonymity).append("', ");

            sqlBuilder.append("'").append(crawlerTime).append("', ");
            sqlBuilder.append("'").append(ProxySite.xici.getDomain()).append("'");
            sqlBuilder.append(")");
            sqlBuilder.append(" on duplicate key update response = " + response);

            sqlList.add(sqlBuilder.toString());
        }

        // Jboot.sendEvent(CrawlerConsts.XICI_EVENT_NAME, sqlList);
    }

    @Override
    public void initConfig() {
        if (spider != null) {
            Configuration conf = Configuration.copyDefault();

            conf.setExecuteInterval(spider.getSleep());
            conf.setMaxExecuteCount(spider.getRetry());
            conf.setConnectTimeout(spider.getTimeout());
            conf.setDefaultUserAgent(spider.getUserAgent());

            // conf.setDefaultCookie("_free_proxy_session=BAh7B0kiD3Nlc3Npb25faWQGOgZFVEkiJWUwYTFkMzVkNmZkMjNkMDIyNzU5YzJiNzE0OGY4ODAxBjsAVEkiEF9jc3JmX3Rva2VuBjsARkkiMWE3Q1Z5R0h5WGpPT1dhWHJrT29VVllFaW8zM055SjRDNk12MTBoRGprb0k9BjsARg");

            this.setConf(conf);
            if (spider.isResumable()) {
                this.setResumable(true);
            }
            this.setThreads(spider.getThread());
            this.addSeedAndReturn(spider.getStartUrl());

            for (int page = START_PAGE; page <= spider.getMaxPageGather(); page++) {
                this.addSeed(String.format(spider.getStartUrl() + "%d", page));
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ProxyXiciCrawler crawler = new ProxyXiciCrawler("xc_crawler", false, null);

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
        // crawler.addRegex("https://www.xicidaili.com/wn/*");

        //crawler.setResumable(true);
        // 设置爬取深度
        crawler.start(1);
    }
}
