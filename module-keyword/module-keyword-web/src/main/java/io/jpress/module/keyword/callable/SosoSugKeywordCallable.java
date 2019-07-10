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

package io.jpress.module.keyword.callable;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.jfinal.kit.Ret;
import io.jboot.Jboot;
import io.jboot.utils.HttpUtil;
import io.jpress.module.keyword.model.status.KeywordSourceStatus;
import io.jpress.module.keyword.model.util.KeywordConsts;
import io.jpress.module.keyword.model.vo.KeywordParamVO;
import io.jpress.module.keyword.model.vo.SosoSugWordsVO;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * 搜狗关键词验证
 *
 * @author Eric.Huang
 * @date 2019-05-20 15:37
 * @package io.jpress.module.crawler.callable
 **/

public class SosoSugKeywordCallable implements Callable<KeywordParamVO> {

    private static final String REL_KEYWORDS_URL = "https://sug.so.360.cn/suggest?encodein=utf-8&encodeout=utf-8&format=json&word=${content}&callback=window.so.sug";

    private KeywordParamVO keyword;

    public SosoSugKeywordCallable(KeywordParamVO keyword) {
        this.keyword = keyword;
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public KeywordParamVO call() throws Exception {

        String url = REL_KEYWORDS_URL.replace("${content}", keyword.getTitle());
        String content = HttpUtil.httpGet(url);
        content = content.replace("window.so.sug(", "")
                .replace(",-1);", "");

        SosoSugWordsVO soso = JSON.parseObject(content, SosoSugWordsVO.class);
        if (soso.getResult() != null && soso.getResult().size() > 0) {

            List<SosoSugWordsVO.Content> result = soso.getResult();
            List<String> keywordList = Lists.newArrayList();
            result.stream().forEach(c -> {
                keywordList.add(c.getWord());
            });

            /** 下拉提示关键词保存 */
            Ret ret = Ret.create();
            ret.put("parentId", keyword.getId());
            ret.put("relWordList", keywordList);
            ret.put("source", KeywordSourceStatus.SUG_COLLECTOR);
            Jboot.sendEvent(KeywordConsts.ADD_KEYWORD_EVENT_NAME, ret);

            keyword.setValid(true);
            return keyword;
        }

        keyword.setValid(false);
        return keyword;
    }

}
