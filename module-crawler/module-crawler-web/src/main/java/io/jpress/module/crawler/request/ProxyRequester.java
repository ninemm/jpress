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

import cn.edu.hfut.dmic.webcollector.model.CrawlDatum;
import cn.edu.hfut.dmic.webcollector.net.Proxies;
import cn.edu.hfut.dmic.webcollector.plugin.net.OkHttpRequester;
import cn.hutool.core.util.RandomUtil;
import com.google.common.collect.Lists;
import com.jfinal.log.Log;
import io.jpress.module.crawler.model.util.CrawlerConsts;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;


/**
 * OkHttp Proxy Requester
 *
 * @author: Eric Huang
 * @date: 2019/6/23 13:32
 */
public class ProxyRequester extends OkHttpRequester {

    private static final Log _LOG = Log.getLog(ProxyRequester.class);

    private String protocol;
    private Proxies proxies;
    private Headers headers;

    public ProxyRequester(String protocol, Headers headers) {

        this.protocol = protocol;
        this.headers = headers;
        proxies = new Proxies();


        /*List<ProxyInfo> list = Aop.get(ProxyInfoService.class).findByProtocol(protocol, 50);
        list.stream().forEach(proxyInfo -> {
            httpsProxies.addHttpProxy(proxyInfo.getIp(), proxyInfo.getPort());
        });*/

        // null means direct connection without proxy
        // proxies.add(null);
    }


    @Override
    public Request.Builder createRequestBuilder(CrawlDatum crawlDatum) {
        return super.createRequestBuilder(crawlDatum)
            .header("User-Agent", RandomUtil.randomEle(CrawlerConsts.USER_AGENT))
            .headers(headers);
            //.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3")
            //.header("Accept-Encoding", "gzip, deflate, br");
    }

    @Override
    public OkHttpClient.Builder createOkHttpClientBuilder() {
        return super.createOkHttpClientBuilder()
            .proxySelector(new ProxySelector() {
                @Override
                public List<Proxy> select(URI uri) {

                    Proxy random = getProxies().randomProxy();
                    List<Proxy> randomProxies = Lists.newArrayList();
                    if (random != null) {
                        randomProxies.add(random);
                    }

                    _LOG.info("random proxies: " + randomProxies);
                    return randomProxies;
                }

                @Override
                public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                    _LOG.info("fail connect host: " + uri.getHost());
                    //Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(uri.getHost(), uri.getPort()));
                    //proxies.remove(proxy);
                }
            });
    }

    public Proxies getProxies() {

        if (protocol.equals("http")) {
            // add http proxy
            proxies.addHttpProxy("222.89.32.170", 9999);
             proxies.addHttpProxy("119.138.225.41", 8118);
            proxies.addHttpProxy("111.177.113.248", 9999);
             proxies.addHttpProxy("116.208.54.139", 9999);
            proxies.addHttpProxy("182.34.33.170", 9999);
            proxies.addHttpProxy("123.249.28.188", 3128);
            proxies.addHttpProxy("1.198.72.73", 9999);
            proxies.addHttpProxy("1.198.72.216", 9999);
            proxies.addHttpProxy("221.4.150.7", 8181);
            proxies.addHttpProxy("113.121.21.187", 9999);
            proxies.addHttpProxy("114.239.235.46", 9999);
            proxies.addHttpProxy("113.121.20.244", 9999);
            proxies.addHttpProxy("1.192.241.137", 9999);
            proxies.addHttpProxy("183.129.207.86", 9999);
            proxies.addHttpProxy("115.221.120.215", 9999);
        } else {
            // add https proxy
            /*proxies.addHttpProxy("163.204.245.211", 9999);
            proxies.addHttpProxy("121.233.207.40", 9999);
            proxies.addHttpProxy("60.211.218.78", 53281);
            proxies.addHttpProxy("114.230.117.31", 9999);
            proxies.addHttpProxy("223.99.214.21", 53281);
            proxies.addHttpProxy("180.119.141.210", 9999);
            proxies.addHttpProxy("47.101.36.129", 8118);
            proxies.addHttpProxy("112.85.170.16", 9999);
            proxies.addHttpProxy("163.204.241.157", 9999);
            proxies.addHttpProxy("113.121.22.85", 9999);
            proxies.addHttpProxy("112.87.70.139", 9999);
            proxies.addHttpProxy("60.13.42.32", 9999);
            proxies.addHttpProxy("175.42.122.220", 9999);
            proxies.addHttpProxy("115.223.115.54", 8010);*/

            proxies.addHttpProxy("122.234.60.168", 8118);
            proxies.addHttpProxy("113.120.37.158", 9999);
            proxies.addHttpProxy("113.120.33.254", 9999);
            proxies.addHttpProxy("182.91.145.15", 9999);

            /*proxies.addHttpProxy("120.83.99.42", 9999);
            proxies.addHttpProxy("175.42.158.93", 9999);
            proxies.addHttpProxy("183.30.204.54", 9000);
            proxies.addHttpProxy("222.189.190.92", 9999);
            proxies.addHttpProxy("58.253.159.101", 9999);
            proxies.addHttpProxy("112.80.41.78", 8888);
            proxies.addHttpProxy("61.128.208.94", 3128);*/
        }
        return proxies;
    }

}
