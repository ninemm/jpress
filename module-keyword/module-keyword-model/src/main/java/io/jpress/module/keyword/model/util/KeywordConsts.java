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

package io.jpress.module.keyword.model.util;

/**
 * 爬虫常量定义
 *
 * @author Eric.Huang
 * @date 2019-05-18 13:34
 * @package io.jpress.module.keyword.model.util
 **/

public class KeywordConsts {

    /** 增加关键词数量 */
    public static final String PLUS_KEYWORD_NUM_EVENT_NAME = "plusKeywordNum";
    /** 减少关键词数量 */
    public static final String MINUS_KEYWORD_NUM_EVENT_NAME = "minusKeywordNum";

    /** 新增关键词 */
    public static final String ADD = "add";
    /** 删除关键词 */
    public static final String MINUS = "minus";

    /** 备份关键词文件路径 */
    public static final String BACKUP_KEYWORDS_PATH = "/backup";

    public static final String SEARCH_ENGINE_BAIDU = "baidu";
    public static final String SEARCH_ENGINE_SOGO = "sogo";
    public static final String SEARCH_ENGINE_360 = "soso";
    public static final String SEARCH_ENGINE_SHENMA = "shenma";

    /** 更新关键词状态(相关词) */
    public static final String UPDATE_REL_WORD_EVENT_NAME = "updateRelWordStatus";

    /** 添加相关关键词 */
    public static final String ADD_KEYWORD_EVENT_NAME = "addKeyword";

    /** 更新关键词状态*/
    public static final String UPDATE_KEYWORD_EVENT_NAME = "updateKeyword";


}
