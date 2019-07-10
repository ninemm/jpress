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

import com.jfinal.aop.Aop;
import com.jfinal.kit.Ret;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import io.jboot.components.event.JbootEvent;
import io.jboot.components.event.JbootEventListener;
import io.jboot.components.event.annotation.EventConfig;
import io.jpress.module.keyword.model.util.KeywordConsts;
import io.jpress.module.keyword.service.KeywordCategoryService;

/**
 * 关键词分类
 *
 * @author Eric.Huang
 * @date 2019-05-18 13:26
 * @package io.jpress.module.crawler.listener
 **/

@EventConfig(action = {KeywordConsts.PLUS_KEYWORD_NUM_EVENT_NAME, KeywordConsts.MINUS_KEYWORD_NUM_EVENT_NAME})
public class KeywordCategoryEventListener implements JbootEventListener {

    private static final Log _LOG = Log.getLog(KeywordCategoryEventListener.class);

    @Override
    public void onEvent(JbootEvent event) {

        Ret param = event.getData();
        Integer num = param.getInt("num");
        String type = param.getStr("type");
        Long categoryId = param.getLong("categoryId");

        String sql = null;
        if (KeywordConsts.ADD.equals(type)) {
            sql = "update c_keyword_category set total_num = total_num + ? where id = ?";
        } else {
            sql = "update c_keyword_category set total_num = total_num - ? where id = ?";
        }

        Db.update(sql, num, categoryId);
        Aop.get(KeywordCategoryService.class).deleteCacheById(categoryId);
    }
}
