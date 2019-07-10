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

package io.jpress.module.keyword.model.status;

import io.jpress.commons.status.BaseStatus;

/**
 * 关键词来源状态
 *
 * @author Eric.Huang
 * @date 2019-05-23 23:30
 * @package io.jpress.module.crawler.model.status
 **/

public class KeywordSourceStatus extends BaseStatus {

    public final static String INPUT = "input";
    public final static String IMPORT = "import";
    public final static String SUG_COLLECTOR = "sug";
    public final static String REL_COLLECTOR = "relate";

    public KeywordSourceStatus() {
        add(INPUT, "录入");
        add(IMPORT, "导入");
        add(SUG_COLLECTOR, "下拉采集");
        add(REL_COLLECTOR, "相关词采集");
    }

    private static KeywordSourceStatus me;

    public static KeywordSourceStatus me() {
        if (me == null) {
            me = new KeywordSourceStatus();
        }
        return me;
    }

}
