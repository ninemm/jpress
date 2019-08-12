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
import io.jboot.Jboot;
import io.jboot.utils.StrUtil;
import io.jpress.module.crawler.enums.ProxySite;
import io.jpress.module.crawler.model.Spider;
import io.jpress.module.crawler.model.util.CrawlerConsts;
import io.jpress.module.crawler.request.ProxyRequester;
import okhttp3.Headers;
import org.joda.time.DateTime;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 站大爷代理IP
 *
 * http://ip.zdaye.com/dayProxy.html
 *
 * http://ip.zdaye.com/dayProxy/2.html
 *
 * @author: Eric Huang
 * @date: 2019/6/20 23:35
 */

public class ProxyZhandayeCrawler extends AbstractBreadthCrawler {

    private Logger LOG = LoggerFactory.getLogger(ProxyZhandayeCrawler.class);

    private static final String URL = "http://ip.zdaye.com/dayProxy.html";
    private static final String LIST_TYPE = "xs_list";
    private static final String CONTENT_TYPE = "xs_content";

    public ProxyZhandayeCrawler(String crawlPath, boolean autoParse, Spider spider) {
        super(crawlPath, autoParse, spider);
        Map<String, String> headerMap = Maps.newHashMap();
        headerMap.put("Host", "ip.zdaye.com");
        headerMap.put("User-Agent", RandomUtil.randomEle(CrawlerConsts.USER_AGENT));
        Headers headers = Headers.of(headerMap);

        // 设置HTTP代理插件
        if (spider != null && spider.isEnableProxy()) {
            setRequester(new ProxyRequester("http", headers));
        }
    }

    @Override
    public void parse(Page page, CrawlDatums next) {
        if (page.matchType(LIST_TYPE)) {

            Elements elements = page.select("div.thread_item > div.thread_content > h3.thread_title");
            elements.stream().forEach(element -> {

                String title = element.text();
                String regex = "(?<year>\\d+)年(?<month>\\d+)月(?<day>\\d+)日";
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(title);

                while (m.find()) {

                    Integer year = Integer.valueOf(m.group("year"));
                    Integer month = Integer.valueOf(m.group("month"));
                    Integer day = Integer.valueOf(m.group("day"));
                    LocalDate date = LocalDate.now();

                    if (year == date.getYear() && month == date.getMonthValue() && day == date.getDayOfMonth()) {
                        String url = element.child(0).absUrl("href");
                        LOG.info("proxy details url: {}", url);
                        next.add(url).type(CONTENT_TYPE);
                    }
                }
            });

        } else if (page.matchType(CONTENT_TYPE)) {
            List<String> sqlList = Lists.newArrayList();
            String crawlerTime = DateTime.now().toString("yyyy-MM-dd HH:mm:ss");
            String[] contents = page.select("div.cont").first().html().split("<br>");

            for (int i = 1; i < contents.length - 1; i++) {
                String content = contents[i].trim();
                if (StrUtil.notBlank(content)) {
                    Pattern p = Pattern.compile(getPatternRegex());
                    Matcher m = p.matcher(content);

                    while (m.find()) {
                        StringBuilder sqlBuilder = new StringBuilder("insert ignore into proxy_info(`ip`, `port`, `location`,");
                        sqlBuilder.append(" `protocol`, `anonymity_type`, `isp`, `crawler_time`, `website`) values(");

                        sqlBuilder.append("'").append(m.group("ip")).append("'").append(", ");
                        sqlBuilder.append(m.group("port")).append(", ");
                        sqlBuilder.append("'").append(m.group("location")).append("'").append(", ");
                        sqlBuilder.append("'").append(m.group("protocol")).append("'").append(", ");

                        sqlBuilder.append("'").append(m.group("anonymity")).append("'").append(", ");
                        sqlBuilder.append("'").append(m.group("isp")).append("'").append(", ");
                        sqlBuilder.append("'").append(crawlerTime).append("'").append(", ");
                        sqlBuilder.append("'").append(ProxySite.xiaoshu.getDomain()).append("'");

                        sqlBuilder.append(")");
                        sqlList.add(sqlBuilder.toString());
                    }
                }
            }

            Jboot.sendEvent(CrawlerConsts.XIAOSHU_EVENT_NAME, sqlList);
        }
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
            this.addSeedAndReturn(URL).type(LIST_TYPE);

            for (int page = START_PAGE; page <= spider.getMaxPageGather(); page++) {
                this.addSeed(String.format("http://ip.zdaye.com/dayProxy/%d.html", page), LIST_TYPE);
            }
        }
    }

    private static String getPatternRegex() {
        StringBuilder sb = new StringBuilder();
        sb.append("(?<ip>\\d+\\.\\d+\\.\\d+\\.\\d+):(?<port>\\d+)@(?<protocol>HTTP[s|S]*)#\\[(?<anonymity>\\S+)\\]");
        sb.append("(?<location>[^\\x00-\\xff]+)\\s(?<isp>[^\\x00-\\xff]+)");
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {

        String URL = "http://ip.zdaye.com/dayProxy.html";
        //URL = "http://www.xsdaili.com/dayProxy/ip/1555.html";
        ProxyZhandayeCrawler crawler = new ProxyZhandayeCrawler("crawler/zdy", false, null);
        Configuration conf = Configuration.copyDefault();
        conf.setExecuteInterval(5000);
        // conf.setThreadKiller(1);
        // 设置爬取URL数量的上限
        //conf.setTopN(5);
        conf.setConnectTimeout(5000);
        conf.setMaxExecuteCount(10);
        conf.setReadTimeout(300000);
        conf.setWaitThreadEndTime(5000);

        conf.setDefaultUserAgent(RandomUtil.randomEle(CrawlerConsts.USER_AGENT));

        crawler.setConf(conf);
        crawler.setThreads(1);
        crawler.addSeedAndReturn(URL).type(LIST_TYPE);

        /*for (int page = START_PAGE; page <= 2; page++) {
            crawler.addSeed(String.format("http://ip.zdaye.com/dayProxy/%d.html", page), LIST_TYPE);
        }*/

        //crawler.setResumable(true);
        //crawler.addRegex("/dayProxy/ip/[0-9]+.html");
        // 设置爬取深度
         crawler.start(2);

        /*String content = "221.223.81.237:8060@HTTP#[高匿名]北京北京 中国联通 ";
        String regex = "(?<ip>\\d+\\.\\d+\\.\\d+\\.\\d+):(?<port>\\d+)";
        Pattern p = Pattern.compile(getPatternRegex());
        Matcher m = p.matcher(content);
        while (m.find()) {
            System.out.println(m.groupCount());
            System.out.println(m.group("ip"));
            System.out.println(m.group("port"));
            System.out.println(m.group("location"));
            System.out.println(m.group("protocol"));
            System.out.println(m.group("anonymity"));
            System.out.println(m.group("isp"));
        }*/
    }
}
