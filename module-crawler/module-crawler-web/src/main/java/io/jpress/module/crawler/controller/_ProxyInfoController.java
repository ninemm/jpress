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
package io.jpress.module.crawler.controller;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import io.jboot.exception.JbootException;
import io.jboot.utils.StrUtil;
import io.jboot.web.controller.annotation.RequestMapping;
import io.jpress.JPressConsts;
import io.jpress.core.menu.annotation.AdminMenu;
import io.jpress.module.crawler.enums.ProxyProtocolType;
import io.jpress.module.crawler.model.ProxyInfo;
import io.jpress.module.crawler.service.ProxyInfoService;
import io.jpress.module.crawler.util.ProxyVerification;
import io.jpress.web.base.AdminControllerBase;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;


@RequestMapping(value = "/admin/crawler/proxyInfo", viewPath = JPressConsts.DEFAULT_ADMIN_VIEW)
public class _ProxyInfoController extends AdminControllerBase {

    @Inject
    private ProxyInfoService proxyService;

    @AdminMenu(text = "代理IP管理", groupId = "crawler", order = 3)
    public void index() {

        keepPara();

        Integer enableCount = proxyService.findCountByEnableStatus(1);
        Integer unableCount = proxyService.findCountByEnableStatus(0);
        setAttr("enableCount", enableCount);
        setAttr("unableCount", unableCount);

        // HTTP 协议
        set("protocols", ProxyProtocolType.values());
        render("crawler/proxy_info_list.html");
    }

    public void paginate() {

        Integer response = getInt("response");
        Integer isEnable = getInt("isEnable", 1);
        String protocol = getPara("protocol");
        String anonymityType = getPara("anonymityType");

        Page<ProxyInfo> page = proxyService.paginate(getPagePara(), getPageSizePara(), protocol, anonymityType,
                response, isEnable);
        Map<String, Object> map = ImmutableMap.of("total", page.getTotalRow(), "rows", page.getList());
        renderJson(map);
    }

    public void edit() {
        int entryId = getParaToInt(0, 0);
        ProxyInfo entry = entryId > 0 ? proxyService.findById(entryId) : null;
        setAttr("proxyInfo", entry);
        render("crawler/proxy_info_edit.html");
    }
   
    public void doSave() {
        ProxyInfo entry = getModel(ProxyInfo.class,"proxyInfo");
        proxyService.saveOrUpdate(entry);
        renderJson(Ret.ok().set("id", entry.getId()));
    }

    public void doDel() {
        Long id = getIdPara();
        render(proxyService.deleteById(id) ? Ret.ok() : Ret.fail());
    }

    public void doVerify() {
        Long id = getIdPara();
        if (id == null) {
            throw new JbootException("proxy id is null.");
        }

        ProxyInfo proxyInfo = proxyService.findById(id);
        boolean isValid = ProxyVerification.me().verifyProxyBySocket(proxyInfo.getIp(), proxyInfo.getPort(), true);
        long currentTime = System.currentTimeMillis();

        if (!isValid) {
            proxyInfo.setInvalidTime(currentTime);
        } else {
            Long lastSurviveTime = proxyInfo.getLastSurviveTime();
            lastSurviveTime = lastSurviveTime != null ? lastSurviveTime : 0L;
            proxyInfo.setLastSurviveTime(currentTime - lastSurviveTime);
            proxyInfo.setInvalidTime(null);
        }

        proxyInfo.setVerified(true);
        proxyInfo.setEnable(isValid);
        proxyInfo.setVerifyTime(new Date());
        proxyService.update(proxyInfo);

        renderOkJson();
    }

    public void doBatchVerify() {

        String ids = getPara("ids");
        if (StrUtil.isBlank(ids)) {
            throw new JbootException("proxy ids is null.");
        }

        final Date verifyTime = new Date();
        List<ProxyInfo> list = Lists.newArrayList();
        Set<String> set = StrUtil.splitToSet(ids, ",");

        set.stream().forEach(id -> {
            ProxyInfo proxyInfo = proxyService.findById(id);
            boolean isValid = ProxyVerification.me().verifyProxyBySocket(proxyInfo.getIp(), proxyInfo.getPort(), true);

            long currentTime = System.currentTimeMillis();
            if (!isValid) {
                proxyInfo.setInvalidTime(currentTime);
            } else {
                Long lastSurviveTime = proxyInfo.getLastSurviveTime();
                lastSurviveTime = lastSurviveTime != null ? lastSurviveTime : 0L;
                proxyInfo.setLastSurviveTime(currentTime - lastSurviveTime);
                proxyInfo.setInvalidTime(null);
            }

            proxyInfo.setVerified(true);
            proxyInfo.setEnable(isValid);
            proxyInfo.setVerifyTime(verifyTime);
            list.add(proxyInfo);
        });

        if (list.size() > 0) {
            proxyService.batchUpdateVerifyStatus(list);
        }

        renderOkJson();
    }
}