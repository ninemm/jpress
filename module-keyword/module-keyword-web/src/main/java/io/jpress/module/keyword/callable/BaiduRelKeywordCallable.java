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

import io.jpress.module.keyword.model.vo.KeywordParamVO;

/**
 * verify keyword in baidu
 * <br /> search relate keyword in baidu, if relate keywords exist then return true, and save the relate keyword
 *
 * @author Eric.Huang
 * @date 2019-05-22 22:15
 * @package io.jpress.module.crawler.callable
 **/

public class BaiduRelKeywordCallable extends AbstractRelKeywordCallable {

    public BaiduRelKeywordCallable(KeywordParamVO keywordParam) {
        super(keywordParam);
    }

    /**
     * Define search url
     *
     * @return java.lang.String
     */
    @Override
    public String getUrl() {
        return String.format("http://www.baidu.com/s?wd=%s", keywordParam.getTitle());
    }

    /**
     * Define css query or xpath
     *
     * @return java.lang.String
     */
    @Override
    public String getXPath() {
        return "#rs>table>tbody>tr>th>a";
    }
}
