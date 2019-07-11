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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jfinal.log.Log;
import io.jboot.Jboot;
import io.jboot.utils.StrUtil;
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
 * 快代理抓取代理IP
 *
 * http://www.kuaidaili.com/free/inha/ <BR/>
 * http://www.kuaidaili.com/free/inha/2/  <BR/>
 * http://www.kuaidaili.com/free/intr/ <BR/>
 * 国外代理目前已经停用
 * http://www.kuaidaili.com/free/outha/ <BR/>
 * http://www.kuaidaili.com/free/outtr/ <BR/>
 *
 * @author: Eric Huang
 * @date: 2019/6/19 22:46
 */
public class ProxyKuaiCrawler extends AbstractBreadthCrawler {

    private static final Log _LOG = Log.getLog(ProxyKuaiCrawler.class);

    private static final Map<String, String> regionType = Maps.newHashMap();
    static {
        regionType.put("inha", "国内高匿");
        regionType.put("intr", "国内普通");
        // regionType.put("outha", "国外高匿");
        // regionType.put("outtr", "国外普通");
    }

    public ProxyKuaiCrawler(String crawlPath, boolean autoParse, Spider spider) {

        super(crawlPath, autoParse, spider);
        Map<String, String> headerMap = Maps.newHashMap();
        Headers headers = Headers.of(headerMap);

        // 设置HTTP代理插件
        if (spider != null && spider.isEnableProxy()) {
            setRequester(new ProxyRequester("https", headers));
        }
    }

    @Override
    public void parse(Page page, CrawlDatums next) {

        List<String> list = Lists.newArrayList();
        String crawlerTime = DateTime.now().toString("yyyy-MM-dd HH:mm:ss");
        Elements elements = page.select("#list>table>tbody>tr");
        for (Element e : elements) {

            String ip = e.selectFirst("td[data-title=\"IP\"]").text();
            String port = e.selectFirst("td[data-title=\"PORT\"]").text();
            String anonymity = e.selectFirst("td[data-title=\"匿名度\"]").text();
            String protocl = e.selectFirst("td[data-title=\"类型\"]").text();

            String location = e.selectFirst("td[data-title=\"位置\"]").text();
            String response = e.selectFirst("td[data-title=\"响应速度\"]").text().replace("秒", "");
            String verifyTime = e.selectFirst("td[data-title=\"最后验证时间\"]").text();
            int lastIndex = location.lastIndexOf(" ");

            // 获取页面内容失败
            if (lastIndex == -1) {
                this.addSeed(page.url());
                continue;
            }
            String isp = location.substring(lastIndex).trim();
            location = location.substring(0, lastIndex);
            if (StrUtil.isBlank(response)) {
                response = "0";
            }

            StringBuilder sqlBuilder = new StringBuilder("insert into proxy_info(`ip`, `port`, `location`, `crawler_time`,");
            sqlBuilder.append(" `response`, `verify_time`, `protocol`, `anonymity_type`, `isp`, `website`) values(");

            sqlBuilder.append("'").append(ip).append("'").append(", ");
            sqlBuilder.append(port).append(", ");
            sqlBuilder.append("'").append(location).append("'").append(", ");
            sqlBuilder.append("'").append(crawlerTime).append("'").append(", ");

            sqlBuilder.append(response).append(", ");
            sqlBuilder.append("'").append(verifyTime).append("'").append(", ");
            sqlBuilder.append("'").append(protocl).append("'").append(", ");
            sqlBuilder.append("'").append(anonymity).append("'").append(", ");

            sqlBuilder.append("'").append(isp).append("'").append(", ");
            sqlBuilder.append("'").append(ProxySite.kuai.getDomain()).append("'");
            sqlBuilder.append(")");
            sqlBuilder.append(" on duplicate key update response = " + response);

            list.add(sqlBuilder.toString());
        }

        Jboot.sendEvent(CrawlerConsts.KUAIPROXY_EVENT_NAME, list);
    }

    @Override
    public void initConfig() {
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

            for (int page = START_PAGE; page <= spider.getMaxPageGather(); page++) {
                this.addSeed(String.format(spider.getStartUrl() + "%d/", page));
            }
        }
    }

    public static void main(String[] args) throws Exception {

        String URL = "https://www.kuaidaili.com/free/inha/";
        ProxyKuaiCrawler kuaiDailiCrawler = new ProxyKuaiCrawler("kdl_crawler", false, null);

        Configuration conf = Configuration.copyDefault();
        conf.setExecuteInterval(3000);
        // conf.setThreadKiller(1);
        // 设置爬取URL数量的上限
        // conf.setTopN(1000);

        kuaiDailiCrawler.setConf(conf);
        kuaiDailiCrawler.setThreads(1);
        kuaiDailiCrawler.setResumable(true);
        // 设置爬取深度
        // kuaiDailiCrawler.start(1);

        String content = "-10";
        int lastIndex = content.lastIndexOf(" ");
        System.out.println(lastIndex);

    }
}
