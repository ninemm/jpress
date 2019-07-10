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
import com.jfinal.log.Log;
import io.jboot.Jboot;
import io.jpress.commons.utils.DateUtils;
import io.jpress.module.crawler.model.Spider;
import io.jpress.module.crawler.model.util.CrawlerConsts;
import io.jpress.module.crawler.request.ProxyRequester;
import okhttp3.Headers;
import org.joda.time.DateTime;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDateTime;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 泥马代理
 *
 * http://www.nimadaili.com/http/
 * http://www.nimadaili.com/https/
 *
 * 定时任务：* /5 * * * *
 *
 * @author: Eric Huang
 * @date: 2019-07-03 17:54
 */
public class ProxyNiMaCrawler extends AbstractBreadthCrawler {

    private static final Log _LOG = Log.getLog(ProxyNiMaCrawler.class);

    private static final Map<String, String> REGION_MAP = Maps.newHashMap();
    static {
        REGION_MAP.put("http", "HTTP代理");
        REGION_MAP.put("https", "HTTPS代理");
    }

    public ProxyNiMaCrawler(String crawlPath, boolean autoParse, Spider spider) {
        super(crawlPath, autoParse, spider);

        Map<String, String> headerMap = Maps.newHashMap();
        headerMap.put("Host", "www.nimadaili.com");
        headerMap.put("User-Agent", RandomUtil.randomEle(CrawlerConsts.USER_AGENT));
        // headerMap.put("Referer", "http://www.nimadaili.com/http/");
        Headers headers = Headers.of(headerMap);

        // 设置HTTP代理插件
        if (spider != null && spider.isEnableProxy()) {
            setRequester(new ProxyRequester("http", headers));
        }
    }

    @Override
    protected void parse(Page page, CrawlDatums next) {
        List<String> proxyList = Lists.newArrayList();
        String crawlerTime = DateTime.now().toString("yyyy-MM-dd HH:mm:ss");
        Elements elements = page.select("table.fl-table > tbody > tr");

        for (Element ele : elements) {
            String ipAndProt = ele.child(0).text();
            List<String> list = StrUtil.splitTrim(ipAndProt, ":");
            String ip = list.get(0);
            String port = list.get(1);

            String protocl = ele.child(1).text();
            String anonymity = ele.child(2).text();
            String location = ele.child(3).text();

            int lastIndex = location.lastIndexOf(" ");
            String isp = location.substring(lastIndex);
            location = location.substring(0, lastIndex);

            String response = ele.child(4).text();
            String verifyTime = getVerifyTime(ele.child(6).text());

            if (StrUtil.isBlank(ip) || StrUtil.isBlank(port) || StrUtil.isBlank(protocl)) {
                continue;
            }

            StringBuilder sqlBuilder = new StringBuilder("insert into proxy_info(`ip`, `port`, `location`, `isp`,");
            sqlBuilder.append(" `response`, `verify_time`, `protocol`, `anonymity_type`, `crawler_time`, `website`) values(");

            sqlBuilder.append("'").append(ip).append("', ");
            sqlBuilder.append(port).append(", ");
            sqlBuilder.append("'").append(location).append("', ");
            sqlBuilder.append("'").append(isp).append("', ");

            sqlBuilder.append(response).append(", ");
            sqlBuilder.append("'").append(verifyTime).append("', ");
            sqlBuilder.append("'").append(protocl).append("', ");
            sqlBuilder.append("'").append(anonymity).append("', ");

            sqlBuilder.append("'").append(crawlerTime).append("', ");
            sqlBuilder.append("'").append("www.nimadaili.com").append("'");
            sqlBuilder.append(")");
            sqlBuilder.append(" on duplicate key update response = " + response);

            //System.out.println(sqlBuilder.append(";").toString());
            proxyList.add(sqlBuilder.toString());
        }
        Jboot.sendEvent(CrawlerConsts.NIMAPROXY_EVENT_NAME, proxyList);
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

            //REGION_MAP.forEach((key, value) -> {
            for (int page = START_PAGE; page <= spider.getMaxPageGather(); page++) {
                this.addSeed(String.format(spider.getStartUrl()  + "%d/", page));
            }
            //});
        }
    }

    private static String getVerifyTime(String verifyTime) {

        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .appendLiteral(' ')
            .append(DateTimeFormatter.ISO_LOCAL_TIME)
            .toFormatter();

        if (StrUtil.isNotBlank(verifyTime)) {
            String regex = "(?<year>\\d+)年(?<month>\\d+)月(?<day>\\d+)日\\s(?<hour>\\d+):(?<minutes>\\d+)";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(verifyTime);

            while (m.find()) {

                Integer year = Integer.valueOf(m.group("year"));
                Integer month = Integer.valueOf(m.group("month"));
                Integer day = Integer.valueOf(m.group("day"));
                Integer hour = Integer.valueOf(m.group("hour"));
                Integer minutes = Integer.valueOf(m.group("minutes"));

                LocalDateTime date = LocalDateTime.of(year, month, day, hour, minutes);
                return date.format(formatter);
            }
        }

        return LocalDateTime.now().format(formatter);
    }

    public static void main(String[] args) throws Exception {

        String startUrl = "http://www.nimadaili.com/http/";
        ProxyNiMaCrawler crawler = new ProxyNiMaCrawler("cawler/nima", false, null);

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

        for (int page = START_PAGE; page <= 2; page++) {
            crawler.addSeed(String.format(startUrl + "%d/", page));
        }
        //crawler.setResumable(true);
        // 设置爬取深度
        // crawler.start(1);
        String str = "2019年7月5日 16:31";

        //Date date = DateUtils.strToDate(str, "yyyy-MM-dd HH:mm");
        //System.out.println(new DateTime(date).toString());
        //str = str.replaceAll("[年|月]", ":0").replace("日", "");
        //Date date = DateUtils.strToDate(str, "yyyy-m-d HH:mm");
        //System.out.println(new DateTime(date).toString());
        //System.out.println(str);
    }
}
