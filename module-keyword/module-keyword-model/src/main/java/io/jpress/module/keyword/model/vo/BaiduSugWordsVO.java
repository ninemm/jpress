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

package io.jpress.module.keyword.model.vo;

import lombok.Data;

import java.util.Arrays;

/**
 * 百度下拉相关词VO
 * <p>
 * window.baidu.sug({q:"关键字",p:false,s:["关键字搜索排名","关键字怎么优化","关键字查询工具","关键字推广","关键词优化","关键词排名","关键字 英文","关键词挖掘","关键词查询","关键词搜索"]});
 * </p>
 * @author Eric.Huang
 * @date 2019-05-20 15:43
 * @package io.jpress.module.crawler.model.vo
 **/

@Data
public class BaiduSugWordsVO {

    private String q;
    private Boolean p;
    private String[] s;

    @Override
    public String toString() {
        return "BaiduSugWordsVO: {" +
                "q='" + q + '\'' +
                ", p=" + p +
                ", s=" + Arrays.toString(s) +
                '}';
    }
}
