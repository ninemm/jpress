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

package io.jpress.module.crawler.callable;

import cn.hutool.core.util.NumberUtil;
import io.jpress.module.crawler.model.vo.ProxyVO;
import io.jpress.module.crawler.util.ProxyVerification;

import java.util.concurrent.Callable;

/**
 * 验证代理
 *
 * @author: Eric Huang
 * @date: 2019/6/27 23:33
 */
public class ProxyValidateCallable implements Callable<ProxyVO> {

    private ProxyVO proxy;

    public ProxyValidateCallable(ProxyVO proxy) {
        this.proxy = proxy;
    }

    @Override
    public ProxyVO call() throws Exception {
        long start = System.currentTimeMillis();
        boolean isValid = ProxyVerification.me().verifyProxyBySocket(proxy.getIp(), proxy.getPort(), true);
        long end = System.currentTimeMillis();

        proxy.setIsEnable(isValid);
        if (isValid) {
            proxy.setResponse(NumberUtil.div((float) (end - start), 1000, 4));
        }

        return proxy;
    }
}
