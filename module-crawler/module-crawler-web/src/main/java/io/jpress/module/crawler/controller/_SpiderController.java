/**
 * Copyright (c) 2016-2019, Michael Yang 杨福海 (fuhai999@gmail.com).
 * <p>
 * Licensed under the GNU Lesser General Public License (LGPL) ,Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jpress.module.crawler.controller;

import com.jfinal.aop.Inject;
import com.jfinal.kit.Ret;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Page;
import io.jboot.exception.JbootException;
import io.jboot.web.controller.annotation.RequestMapping;
import io.jboot.web.validate.EmptyValidate;
import io.jboot.web.validate.Form;
import io.jpress.JPressConsts;
import io.jpress.core.menu.annotation.AdminMenu;
import io.jpress.module.crawler.crawler.*;
import io.jpress.module.crawler.enums.ProxySite;
import io.jpress.module.crawler.model.Spider;
import io.jpress.module.crawler.service.SpiderService;
import io.jpress.web.base.AdminControllerBase;

import java.util.Date;


@RequestMapping(value = "/admin/crawler/spider", viewPath = JPressConsts.DEFAULT_ADMIN_VIEW)
public class _SpiderController extends AdminControllerBase {

    private static final Log _LOG = Log.getLog(_SpiderController.class);

    @Inject
    private SpiderService spiderService;

    @AdminMenu(text = "网站爬虫模板", groupId = "crawler", order = 0)
    public void index() {
        Page<Spider> page = spiderService.paginate(getPagePara(), 10);
        setAttr("page", page);
        render("crawler/spider_list.html");
    }

    @AdminMenu(text = "新增代理模板", groupId = "crawler", order = 1)
    public void add() {
        Page<Spider> page = spiderService.paginate(getPagePara(), 10);
        setAttr("page", page);
        render("crawler/proxy_ip_edit.html");
    }
   
    public void edit() {
        int entryId = getParaToInt(0, 0);

        Spider entry = entryId > 0 ? spiderService.findById(entryId) : null;
        setAttr("spider", entry);
        render("crawler/spider_edit.html");
    }

    @EmptyValidate({
        @Form(name = "spider.site_name", message = "网站名称不能为空"),
        @Form(name = "spider.domain", message = "网站域名不能为空"),
        @Form(name = "spider.start_url", message = "起始链接不能为空")
    })
    public void doSave() {
        Spider entry = getModel(Spider.class,"spider");
        spiderService.saveOrUpdate(entry);
        renderJson(Ret.ok().set("id", entry.getId()));
    }

    public void doDel() {
        Long id = getIdPara();
        render(spiderService.deleteById(id) ? Ret.ok() : Ret.fail());
    }

    public void task() {
        keepPara();
        render("crawler/spider_task.html");
    }

    public void start() {
        Object id = getPara(0);
        if (id == null) {
            throw new JbootException("id 不能为空");
        }

        Spider spider = spiderService.findById(id);
        spider.setIsStartCrawler(true);

        if (spiderService.update(spider)) {
            int depth = 1;
            String key = null;
            AbstractBreadthCrawler crawler = null;
            // ProxyCrawlerManager.me().
            if (spider.getStartUrl().contains(ProxySite.kuaiProxy.getKey())) {
                key = ProxySite.kuaiProxy.getKey();
                crawler = new ProxyKuaiCrawler("crawler/kuai", false, spider);
            } else if (spider.getStartUrl().contains(ProxySite.xsProxy.getKey())) {
                depth = 2;
                key = ProxySite.xsProxy.getKey();
                crawler = new ProxyXiaoShuCrawler("crawler/xiaoshu", false, spider);
            } else if (spider.getStartUrl().contains(ProxySite.xiciProxy.getKey())) {
                key = ProxySite.xiciProxy.getKey();
                crawler = new ProxyXiciCrawler("crawler/xici", false, spider);
            } else if (spider.getStartUrl().contains(ProxySite.nima.getKey())) {
                key = ProxySite.nima.getKey();
                crawler = new ProxyNiMaCrawler("crawler/nima", false, spider);
            }

            try {
                if (crawler != null) {
                    ProxyCrawlerManager.me().start(key, crawler, depth);
                }
            } catch (Exception e) {
                _LOG.error("crawler:" + crawler.getClass().getName() + " start failure. ", e);
                renderFailJson();
                return ;
            }

            renderOkJson();
        }
        renderFailJson();
    }

    public void stop() {

        Object id = getPara(0);
        if (id == null) {
            throw new JbootException("id 不能为空");
        }

        Spider spider = spiderService.findById(id);
        spider.setIsStartCrawler(false);

        if (spiderService.update(spider)) {
            if (spider.getStartUrl().contains(ProxySite.kuaiProxy.getKey())) {
                ProxyCrawlerManager.me().stop(ProxySite.kuaiProxy.getKey());
            } else if (spider.getStartUrl().contains(ProxySite.xsProxy.getKey())) {
                ProxyCrawlerManager.me().stop(ProxySite.kuaiProxy.getKey());
            } else if (spider.getStartUrl().contains(ProxySite.xiciProxy.getKey())) {
                ProxyCrawlerManager.me().stop(ProxySite.xiciProxy.getKey());
            } else if (spider.getStartUrl().contains(ProxySite.nima.getKey())) {
                ProxyCrawlerManager.me().stop(ProxySite.nima.getKey());
            }
        }
        renderOkJson();
    }
}