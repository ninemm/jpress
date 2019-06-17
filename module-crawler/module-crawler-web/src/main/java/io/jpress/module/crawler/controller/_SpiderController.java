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

import cn.edu.hfut.dmic.webcollector.conf.Configuration;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import io.jboot.exception.JbootException;
import io.jboot.web.controller.annotation.RequestMapping;
import io.jboot.web.validate.EmptyValidate;
import io.jboot.web.validate.Form;
import io.jpress.JPressConsts;
import io.jpress.core.menu.annotation.AdminMenu;
import io.jpress.module.crawler.crawler.BaiduKeywordCrawler;
import io.jpress.module.crawler.model.Spider;
import io.jpress.module.crawler.service.SpiderService;
import io.jpress.web.base.AdminControllerBase;

import java.util.Date;


@RequestMapping(value = "/admin/crawler/spider", viewPath = JPressConsts.DEFAULT_ADMIN_VIEW)
public class _SpiderController extends AdminControllerBase {

    @Inject
    private SpiderService spiderService;

    @AdminMenu(text = "采集模板", groupId = "crawler", order = 1)
    public void index() {
        Page<Spider> page = spiderService.paginate(getPagePara(), 10);
        setAttr("page", page);
        render("crawler/spider_list.html");
    }

   
    public void edit() {
        int entryId = getParaToInt(0, 0);

        Spider entry = entryId > 0 ? spiderService.findById(entryId) : null;
        setAttr("spider", entry);
        set("now",new Date());
        render("crawler/spider_edit.html");
    }

    @EmptyValidate({
        @Form(name = "site_name", message = "网站名称不能为空"),
        @Form(name = "domain", message = "网站域名不能为空"),
        @Form(name = "start_url", message = "起始链接不能为空")
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


    public void start() {
        Object id = getPara(0);
        if (id == null) {
            throw new JbootException("id 不能为空");
        }

        Spider spider = spiderService.findById(id);
        Configuration conf = Configuration.copyDefault();
        conf.setConnectTimeout(spider.getTimeout());
        conf.setExecuteInterval(spider.getSleep());
        conf.setTopN(spider.getMaxPageGather());

        conf.setThreadKiller(spider.getThread());

        BaiduKeywordCrawler baiduCrawler = new BaiduKeywordCrawler("crawler", true);
        baiduCrawler.addSeed(spider.getStartUrl());
        baiduCrawler.setConf(conf);
        baiduCrawler.start();

    }
}