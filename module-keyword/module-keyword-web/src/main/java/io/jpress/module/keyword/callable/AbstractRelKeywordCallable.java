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

import com.jfinal.kit.Ret;
import com.jfinal.log.Log;
import io.jboot.Jboot;
import io.jboot.utils.HttpUtil;
import io.jpress.module.keyword.model.status.KeywordSourceStatus;
import io.jpress.module.keyword.model.util.KeywordConsts;
import io.jpress.module.keyword.model.vo.KeywordParamVO;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.util.concurrent.Callable;

/**
 * handle relate keyword of thread
 *
 * @author Eric.Huang
 * @date 2019-05-22 22:36
 * @package io.jpress.module.crawler.callable
 **/

public abstract class AbstractRelKeywordCallable implements Callable<KeywordParamVO> {

    private static final Log _LOG = Log.getLog(AbstractRelKeywordCallable.class);

    protected KeywordParamVO keywordParam;

    public AbstractRelKeywordCallable(KeywordParamVO keywordParam) {
        this.keywordParam = keywordParam;
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public KeywordParamVO call() throws Exception {
        String url = String.format(getUrl(), keywordParam.getTitle());
        String content = HttpUtil.httpGet(url);
        Elements elements = Jsoup.parseBodyFragment(content).select(getXPath());

        if (elements != null && elements.size() > 0) {

            _LOG.info("search relate keyword num : " + elements.size());
            /**
             * save relate keywords
             */
            Ret ret = Ret.create();
            ret.put("parentId", keywordParam.getId());
            ret.put("source", KeywordSourceStatus.REL_COLLECTOR);
            ret.put("relWordList", elements.eachText());
            Jboot.sendEvent(KeywordConsts.ADD_KEYWORD_EVENT_NAME, ret);

            keywordParam.setValid(true);
            return keywordParam;
        }

        keywordParam.setValid(false);
        return keywordParam;
    }

    /**
     * Define search url
     * @param
     * @return java.lang.String
     */
    public abstract String getUrl();

    /**
     * Define css query or xpath
     * @param
     * @return java.lang.String
     */
    public abstract String getXPath();
}
