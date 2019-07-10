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

package io.jpress.module.crawler.enums;

/**
 * Proxy Anonymity Type Enum
 *
 * @author: Eric Huang
 * @date: 2019/6/23 22:44
 */
public enum ProxyAnonymityType {
    // 透明
    transparent(1, "transparent"),
    // 匿名
    anonymous(2, "anonymity"),
    // 混淆
    distorting(3, "distorting"),
    // 高匿
    elite(4, "elite");

    private int key;
    private String type;

    ProxyAnonymityType(int key, String type) {
        this.key = key;
        this.type = type;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
