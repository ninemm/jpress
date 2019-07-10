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

package io.jpress.module.keyword.listener;

import com.google.common.collect.Lists;
import com.jfinal.aop.Aop;
import com.jfinal.kit.Ret;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import io.jboot.components.event.JbootEvent;
import io.jboot.components.event.JbootEventListener;
import io.jboot.components.event.annotation.EventConfig;
import io.jpress.commons.utils.DateUtils;
import io.jpress.commons.utils.WebSocketUtil;
import io.jpress.module.keyword.model.util.KeywordConsts;
import io.jpress.module.keyword.model.vo.KeywordParamVO;
import io.jpress.module.keyword.service.KeywordService;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;

/**
 * 处理搜索框下拉提示
 *
 * @author Eric.Huang
 * @date 2019-05-20 22:55
 * @package io.jpress.module.crawler.listener
 **/

@EventConfig(action = KeywordConsts.UPDATE_KEYWORD_EVENT_NAME)
public class UpdateKeywordEventListener implements JbootEventListener {

    private static final Log _LOG = Log.getLog(UpdateKeywordEventListener.class);

    @Override
    public void onEvent(JbootEvent event) {

        Ret ret = event.getData();
        String taskId = ret.getStr("taskId");
        Map<String, List<KeywordParamVO>> keywordData = (Map<String, List<KeywordParamVO>>) ret.get("keywordData");

        Object modified = DateTime.now().toString(DateUtils.DEFAULT_FORMATTER);
        List<String> sqlList = Lists.newArrayList();
        KeywordService keywordService = Aop.get(KeywordService.class);

        keywordData.forEach((searchType, list) -> {
            list.stream().forEach(keyword -> {

                StringBuilder sql = new StringBuilder("update c_keyword set modified = '").append(modified).append("'");
                int enabled = keyword.getValid() ? 1 : 0;

                if (searchType.equals(KeywordConsts.SEARCH_ENGINE_BAIDU)) {
                    sql.append(", is_baidu_enabled = " + enabled);
                    sql.append(", is_baidu_checked = 1 where id = " + keyword.getId());
                } else if (searchType.equals(KeywordConsts.SEARCH_ENGINE_SOGO)) {
                    sql.append(", is_sogo_enabled = " + enabled);
                    sql.append(", is_sogo_checked = 1 where id = " + keyword.getId());
                } else if (searchType.equals(KeywordConsts.SEARCH_ENGINE_360)) {
                    sql.append(", is_soso_enabled = " + enabled);
                    sql.append(", is_soso_checked = 1 where id = " + keyword.getId());
                } else if (searchType.equals(KeywordConsts.SEARCH_ENGINE_SHENMA)) {
                    sql.append(", is_shenma_enabled = " + enabled);
                    sql.append(", is_shenma_checked = 1 where id = " + keyword.getId());
                }

                sqlList.add(sql.toString());
                _LOG.debug("关键词ID: " + keyword.getId() + ", 关键词名称: " + keyword.getTitle());
                String message = "关键词ID: " + keyword.getId() + ", 关键词名称: " + keyword.getTitle();
                WebSocketUtil.sendMessage(taskId, message);
            });
        });

        Db.batch(sqlList, sqlList.size());
    }

}
