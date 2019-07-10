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
 * Request Protocol Type Enum
 *
 * @author: Eric Huang
 * @date: 2019/6/23 22:45
 */
public enum ProxyProtocolType {
    http(1, "http"),
    https(2, "https"),
    socks4(3, "socks4"),
    socks5(4, "socks5"),
    //不区分socks4或5
    socks(5, "socks");

    private int key;
    private String requestType;

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    ProxyProtocolType(int key, String requestType) {
        this.key = key;
        this.requestType = requestType;
    }

}
