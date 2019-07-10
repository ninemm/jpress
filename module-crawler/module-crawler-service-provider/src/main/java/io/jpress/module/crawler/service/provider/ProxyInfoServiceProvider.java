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

package io.jpress.module.crawler.service.provider;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import io.jboot.aop.annotation.Bean;
import io.jboot.db.model.Columns;
import io.jboot.service.JbootServiceBase;
import io.jboot.utils.StrUtil;
import io.jpress.module.crawler.model.ProxyInfo;
import io.jpress.module.crawler.service.ProxyInfoService;

import java.util.List;

@Bean
public class ProxyInfoServiceProvider extends JbootServiceBase<ProxyInfo> implements ProxyInfoService {

    @Override
    public Page<ProxyInfo> paginate(int page, int pageSize, String protocol, String anonymityType,
            Integer response, Integer isEnable) {

        Columns columns = Columns.create();
        if (StrUtil.notBlank(protocol)) {
            columns.add("protocol", protocol);
        }

        if (StrUtil.notBlank(anonymityType)) {
            columns.add("anonymity_type", anonymityType);
        }

        if (response != null) {
            columns.lt("response", response);
        }

        if (isEnable != null) {
            columns.add("is_enable", isEnable);
        }

        return DAO.paginateByColumns(page, pageSize, columns, "verify_time desc");
    }

    @Override
    public Page<ProxyInfo> paginate(int page, int pageSize, String orderBy) {
        Columns columns = Columns.create();
        // columns.add("is_enable", 0);
        return DAO.paginate(page, pageSize, orderBy);
    }

    @Override
    public void batchSave(List<String> sqlList) {
        Db.batch(sqlList, sqlList.size());
    }

    @Override
    public void batchUpdateVerifyStatus(List<ProxyInfo> list) {
        Db.batchUpdate(list, list.size());
    }

    @Override
    public Integer findCountByEnableStatus(Integer isEnable) {
        return Db.queryInt("select count(*) from proxy_info where is_enable = ?", isEnable);
    }

    @Override
    public List<ProxyInfo> findByProtocol(String protocol, Integer count) {
        Columns columns = Columns.create("protocol", protocol);
        columns.lt("response", 1);
        return DAO.findListByColumns(columns, count);
    }

    private String genReplaceIntoSql(ProxyInfo proxyInfo) {
        StringBuilder sqlBuilder = new StringBuilder("replace into proxy_info(`ip`, `port`, `location`, `response`,");
        sqlBuilder.append(" `verify_time`, `protocol`, `anonymity_type`, `isp`, `method`, `username`, `password`) values(");

        sqlBuilder.append("'").append(proxyInfo.getIp()).append("'").append(", ");
        sqlBuilder.append("'").append(proxyInfo.getPort()).append("'").append(", ");
        sqlBuilder.append("'").append(proxyInfo.getLocation()).append("'").append(", ");
        sqlBuilder.append("'").append(proxyInfo.getResponse()).append("'").append(", ");

        sqlBuilder.append("'").append(proxyInfo.getVerifyTime()).append("'").append(", ");
        sqlBuilder.append("'").append(proxyInfo.getProtocol()).append("'").append(", ");
        sqlBuilder.append("'").append(proxyInfo.getAnonymityType()).append("'").append(", ");
        sqlBuilder.append("'").append(proxyInfo.getIsp()).append("'").append(", ");

        sqlBuilder.append("'").append(proxyInfo.getMethod()).append("'").append(", ");
        sqlBuilder.append("'").append(proxyInfo.getUsername()).append("'").append(", ");
        sqlBuilder.append("'").append(proxyInfo.getPassword()).append("'");
        sqlBuilder.append(")");
        return sqlBuilder.toString();
    }

}