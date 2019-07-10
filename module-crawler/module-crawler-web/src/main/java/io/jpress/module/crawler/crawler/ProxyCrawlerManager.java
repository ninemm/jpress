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
import cn.edu.hfut.dmic.webcollector.crawler.Crawler;
import com.jfinal.log.Log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 代理爬虫池
 *
 * @author: Eric Huang
 * @date: 2019/6/23 12:01
 */
public class ProxyCrawlerManager {

    private static final Log LOG = Log.getLog(ProxyCrawlerManager.class);

    private static final ProxyCrawlerManager manager = new ProxyCrawlerManager();

    private Map<String, AbstractBreadthCrawler> crawlerMap = new ConcurrentHashMap<>();

    public ProxyCrawlerManager(){
        init();
    }

    public static final ProxyCrawlerManager me(){
        return manager;
    }

    private void init() {}

    public void addCrawlerTask(String crawlerId, AbstractBreadthCrawler crawler) {
        if (crawlerMap.containsKey(crawlerId)) {
            throw new IllegalStateException("Crawler:" + crawlerId + " is already in the current crawler map.");
        }

        crawlerMap.put(crawlerId, crawler);
    }

    public void addCrawlerTask(String crawlerId, AbstractBreadthCrawler crawler, Configuration conf) {
        if (crawlerMap.containsKey(crawlerId)) {
            throw new IllegalStateException("Crawler:" + crawlerId + " is already in the current crawler map.");
        }

        crawler.setConf(conf);
        addCrawlerTask(crawlerId, crawler);
    }

    public void start(String crawlerId) throws Exception {
        final AbstractBreadthCrawler crawler = crawlerMap.get(crawlerId);

        if (crawler != null && crawler.getStatus() != Crawler.RUNNING) {
            crawler.start(1);
        }
    }

    public void start(String crawlerId, int depth) throws Exception {
        final AbstractBreadthCrawler crawler = crawlerMap.get(crawlerId);

        if (crawler != null && crawler.getStatus() != Crawler.RUNNING) {
            crawler.start(depth);
        }
    }

    public void start(String crawlerId, AbstractBreadthCrawler crawler) throws Exception {

        AbstractBreadthCrawler persiste = crawlerMap.get(crawlerId);
        if (persiste == null) {
            addCrawlerTask(crawlerId, crawler);
            crawler.start(1);
            return;
        }

        if (persiste != null && persiste.getStatus() != Crawler.RUNNING) {
            persiste.start(1);
        }
    }

    public void start(String crawlerId, AbstractBreadthCrawler crawler, int depth) throws Exception {

        AbstractBreadthCrawler persiste = crawlerMap.get(crawlerId);
        if (persiste == null) {
            addCrawlerTask(crawlerId, crawler);
            crawler.start(depth);
            return;
        }

        if (crawler != null && crawler.getStatus() != Crawler.RUNNING) {
            crawler.start(depth);
        }
    }

    public void stop(String crawlerId) {
        final AbstractBreadthCrawler crawler = crawlerMap.get(crawlerId);
        if (crawler != null && crawler.getStatus() == Crawler.RUNNING) {
            crawler.stop();
        }
    }

}
