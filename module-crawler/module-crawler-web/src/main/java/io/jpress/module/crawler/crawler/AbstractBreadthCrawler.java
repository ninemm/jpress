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

import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;
import cn.edu.hfut.dmic.webcollector.util.ExceptionUtils;
import io.jpress.module.crawler.model.Spider;
import org.apache.http.HttpStatus;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * BreadthCrawler 抽象类
 *
 * @author: Eric Huang
 * @date: 2019/6/25 17:57
 */
public abstract class AbstractBreadthCrawler extends BreadthCrawler {

    protected static final int START_PAGE = 2;

    protected Spider spider;
    public AbstractBreadthCrawler(String crawlPath, boolean autoParse, Spider spider) {
        super(crawlPath, autoParse);
        this.spider = spider;
        initConfig();
    }

    @Override
    public void visit(Page page, CrawlDatums next) {
        if (page.code() == HttpStatus.SC_MOVED_PERMANENTLY
                || page.code() == HttpStatus.SC_MOVED_PERMANENTLY) {

            try {
                String redirectUrl = new URL(new URL(page.url()), page.location()).toExternalForm();
                next.addAndReturn(redirectUrl);
            } catch (MalformedURLException e) {
                e.printStackTrace();

            }
            return;
        }

        if (page.code() == HttpStatus.SC_SERVICE_UNAVAILABLE) {
            next.add(page.url());
            return;
        }

        //try {
            parse(page, next);
        //} catch (Exception e) {
        //    // 捕捉到异常时，即认为这个网页需要重新爬取时
        //    ExceptionUtils.fail(e);
        //}
    }

    /**
     * 解析网页内容
     *
     * @author Eric
     * @date 17:59 2019/6/25
     * @param page
     * @param next
     * @return
     */
    protected abstract void parse(Page page, CrawlDatums next);

    /**
     * 初始化爬虫配置
     *
     * @author Eric
     * @date 18:17 2019/6/25
     */
    protected abstract void initConfig();

    /**
     * 返回爬虫状态
     *
     * 0 - INIT
     * 1 - RUNNING
     * 2 - STOPPED
     *
     * @author Eric
     * @date 22:44 2019/6/26
     * @return
     */
    public int getStatus() {
        return status;
    }

}
