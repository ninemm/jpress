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

package io.jpress.module.crawler.listener;

import com.google.common.collect.Lists;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import io.jboot.components.event.JbootEvent;
import io.jboot.components.event.JbootEventListener;
import io.jboot.components.event.annotation.EventConfig;
import io.jpress.commons.utils.DateUtils;
import io.jpress.module.crawler.model.util.CrawlerConsts;
import io.jpress.module.crawler.model.vo.ProxyVO;
import org.joda.time.DateTime;

import java.util.List;

/**
 * 处理快代理 IP
 *
 * @author: Eric Huang
 * @date: 2019/6/20 22:34
 */

@EventConfig(action = { CrawlerConsts.VALIDATE_PROXY_EVENT_NAME })
public class ProxyValidateEventListener implements JbootEventListener {
    private static final Log LOG = Log.getLog(ProxyValidateEventListener.class);

    @Override
    public void onEvent(JbootEvent event) {

        List<String> sqlList = Lists.newArrayList();
        List<ProxyVO> list = event.getData();
        String modified = DateTime.now().toString(DateUtils.DEFAULT_FORMATTER);

        list.stream().forEach(proxy -> {
            int isEnable = proxy.getIsEnable() ? 1 : 0;
            StringBuilder sqlBuilder = new StringBuilder("update proxy_info set");
            sqlBuilder.append(" is_verified = ").append(1);
            sqlBuilder.append(", is_enable = ").append(proxy.getIsEnable());

            if (proxy.getResponse() != null) {
                sqlBuilder.append(", response = ").append(proxy.getResponse());
                sqlBuilder.append(", verify_time = ").append("'").append(modified).append("'");
            }
            sqlBuilder.append(", modified = ").append("'").append(modified).append("'");

            sqlBuilder.append(" where ip = ").append("'").append(proxy.getIp()).append("'");
            sqlBuilder.append(" and port = ").append(proxy.getPort());
            sqlList.add(sqlBuilder.toString());
        });
        LOG.info("batch update proxy enable status, proxy size: " + sqlList.size());
        Db.batch(sqlList, sqlList.size());
    }
}
