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

package io.jpress.module.crawler.request;

import com.google.common.collect.Maps;
import lombok.Data;

import java.util.Map;

/**
 * Http Request
 *
 * @author: Eric Huang
 * @date: 2019/6/26 17:30
 */
@Data
public class Request {

    private String method;
    private String charset;
    private Map<String, String> headers = Maps.newHashMap();
    private Map<String, Object> params = Maps.newHashMap();

    public Request setHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }

}
