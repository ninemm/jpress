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

package io.jpress.module.crawler.util;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;
import com.jfinal.log.Log;
import io.jboot.utils.StrUtil;
import io.jpress.module.crawler.model.util.CrawlerConsts;
import io.jpress.module.crawler.request.HttpClientUtil;
import io.jpress.module.crawler.request.Response;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

/**
 * 代理验证
 *
 * @author: Eric Huang
 * @date: 2019/6/24 17:46
 */
public class ProxyVerification {

    private final static Logger _LOG = LoggerFactory.getLogger(ProxyVerification.class);

    private final static int CONNECT_NUM = 2;
    private final static int CONNECT_TIMEOUT = 5000;

    private static final ProxyVerification PROXY_VALIDATE = new ProxyVerification();

    public static final ProxyVerification me() {
        return PROXY_VALIDATE;
    }

    private static final List<String> websites = Lists.newArrayList();

    static {
        websites.add("https://www.sina.com.cn");
        websites.add("https://www.qq.com");
    }

    /** 代理布隆过滤器 */
    private final BloomFilter<String> proxyBloomFilter = BloomFilter.create(new Funnel<String>() {
        @Override
        public void funnel(String proxy, PrimitiveSink primitiveSink) {
            primitiveSink.putString(proxy, Charsets.UTF_8);
        }
    }, 1024 * 1024 * 32, 0.00000001d);

    public boolean verifyProxyBySocket(String ip, Integer port, boolean duplicate) {

        if (StrUtil.isBlank(ip) || port == null) {
            return false;
        }

        if (duplicate && isVerified(ip, port)) {
            return false;
        }

        try {
            long start = System.currentTimeMillis();
            Socket socket = null;
            // 失败重试2次
            // for (int i = 0; i < CONNECT_NUM; i++) {
            try {
                socket = new Socket();
                InetSocketAddress endpointSocketAddr = new InetSocketAddress(ip, port);
                socket.connect(endpointSocketAddr, CONNECT_TIMEOUT);
                long end = System.currentTimeMillis();
                _LOG.info("proxy connect success, and spend time: " + (end - start) + "ms");
                return true;
            } catch (IOException e) {
                _LOG.error("socket connect exception " + e.getMessage() + ", proxy: " + ip + ":" + port, e);
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        _LOG.error("socket close error.", e);
                    }
                }
            }
            // }
            long end = System.currentTimeMillis();
            _LOG.info("proxy connect failure, and spend time: " + (end - start) + "ms");
            return false;
        } finally {
            if (duplicate) {
                addProxy(ip, port);
            }
        }
    }

    public boolean verifyProxyByBaidu(String ip, Integer port, boolean duplicate) {

        if (StrUtil.isBlank(ip) || port == null) {
            return false;
        }

        if (duplicate && isVerified(ip, port)) {
            return false;
        }

        String url = "https://www.baidu.com/";
        CloseableHttpClient closeableHttpClient = null;
        HttpClientUtil httpClientUtil = HttpClientUtil.getInstance();

        try {
            HttpHost proxy = new HttpHost(ip, port);
            closeableHttpClient = httpClientUtil.createHttpClient(CONNECT_TIMEOUT, proxy,null);
            Response response = httpClientUtil.getResponse(closeableHttpClient, url);

            if (response != null) {
                if (response.getStatusCode() == HttpStatus.SC_OK) {
                    return true;
                }
            }

            return false;
        } finally {
            if (duplicate) {
                addProxy(ip, port);
            }
        }
    }

    public boolean verifyProxyByHttp(String ip, Integer port, boolean duplicate) {

        if (StrUtil.isBlank(ip) || port == null) {
            return false;
        }

        if (duplicate && isVerified(ip, port)) {
            return false;
        }

        long start = System.currentTimeMillis();
        boolean isOk = false;

        CloseableHttpClient closeableHttpClient = null;
        try {
            HttpHost proxy = new HttpHost(ip, port);
            DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
            closeableHttpClient = HttpClients.custom().setUserAgent(CrawlerConsts.USER_AGENT[2])
                    .setRoutePlanner(routePlanner)
                    .build();

            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(30000)
                    .setConnectTimeout(CONNECT_TIMEOUT)
                    .build();

            HttpGet get = new HttpGet(websites.remove(0));
            get.setConfig(requestConfig);
            HttpResponse response = closeableHttpClient.execute(get);
            StatusLine status = response.getStatusLine();
            isOk = status.getStatusCode() == 200;
        } catch (Exception e) {
            _LOG.error("Http Get failure.", e);
        } finally {
            if (closeableHttpClient != null) {
                try {
                    closeableHttpClient.close();
                } catch (IOException e) {
                    _LOG.error("close http client failure.", e);
                }
            }

            if (duplicate) {
                addProxy(ip, port);
            }
        }
        long end = System.currentTimeMillis();
        long response = end - start;

        return isOk;
    }

    private boolean isVerified(String ip, Integer port) {
        String proxy = new StringBuilder(ip).append(":").append(port.intValue()).toString();
        if (proxyBloomFilter.mightContain(proxy)) {
            _LOG.info("Proxy: " + proxy + " has verified.");
            return true;
        }
        return false;
    }

    private void addProxy(String ip, Integer port) {
        String proxy = new StringBuilder(ip).append(":").append(port.intValue()).toString();
        proxyBloomFilter.put(proxy);
    }

    public static void main(String[] args) {
        String ip = "27.46.23.229";
        int port = 8888;

        //ip = "61.128.208.94";
        //port = 3128;

        boolean isValid = ProxyVerification.me().verifyProxyBySocket(ip, port, true);
        System.out.println(isValid);
        //boolean isValid = ProxyVerification.me().verifyProxyByBaidu(ip, port, true);
        //System.err.println(isValid);
    }
}
