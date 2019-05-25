package io.jpress.module.crawler.proxy;


import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Random;

/**
 * 随机代理
 *
 * @author Eric.Huang
 * @date 2019-05-25 23:53
 * @package io.jpress.module.crawler.proxy
 **/

public class RandomProxyGenerator implements ProxyGenerator {

    protected Proxys proxys = null;

    public RandomProxyGenerator() {
        proxys = new Proxys();
    }

    public RandomProxyGenerator(Proxys proxys) {
        this.proxys = proxys;
    }

    protected final Object lock = new Object();

    public void addProxy(Proxy proxy) {
        synchronized (lock) {
            proxys.add(proxy);
        }
    }

    public void removeProxy(Proxy proxy) {
        synchronized (lock) {
            proxys.remove(proxy);
        }
    }

    Random random = new Random();

    @Override
    public Proxy next(String url) {
        synchronized (lock) {
            if (proxys == null) {
                return null;
            }
            if (proxys.isEmpty()) {
                return null;
            }
            if (proxys.size() == 1) {
                return proxys.get(0);
            }
            try {
                int r = random.nextInt(proxys.size());
                return proxys.get(r);
            } catch (Exception ex) {
                return null;
            }
        }
    }

    public Proxys getProxys() {
        return proxys;
    }

    public void setProxys(Proxys proxys) {
        this.proxys = proxys;
    }

    public void addProxy(String host, int port, Proxy.Type type) {
        addProxy(new Proxy(type, new InetSocketAddress(host, port)));
    }

    public void addProxy(String host, int port) {
        addProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port)));
    }

    @Override
    public void markBad(Proxy proxy, String url) {

    }

    @Override
    public void markGood(Proxy proxy, String url) {
    }
}
