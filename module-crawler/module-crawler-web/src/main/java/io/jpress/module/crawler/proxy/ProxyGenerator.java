package io.jpress.module.crawler.proxy;

import java.net.Proxy;

/**
 * 随机代理
 *
 * @author Eric.Huang
 * @date 2019-05-25 23:54
 * @package io.jpress.module.crawler.proxy
 **/

public interface ProxyGenerator {
    public Proxy next(String url);
    public void markGood(Proxy proxy,String url);
    public void markBad(Proxy proxy,String url);
}
