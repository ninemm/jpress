package io.jpress.module.crawler.proxy;

import com.jfinal.log.Log;
import io.jpress.module.crawler.util.HttpStatus;
import io.jpress.module.crawler.util.ProxyPersistUtils;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.*;

/**
 * 代理池管理
 *
 * @author Eric.Huang
 * @date 2019-05-25 23:34
 * @package io.jpress.module.crawler.proxy
 **/

public class ProxyPoolManager {

    private static final Log _LOG = Log.getLog(ProxyPoolManager.class);

    /** 存储空闲的Proxy */
    private BlockingQueue<HttpProxy> idleQueue = new DelayQueue<HttpProxy>();
    /** 存储所有的Proxy */
    private Map<String, HttpProxy> totalQueue = new ConcurrentHashMap<String, HttpProxy>();

    public ProxyPoolManager() {
        final ProxyPersistUtils proxyAutoSave = new ProxyPersistUtils();

        // 读取上次的Proxy记录
        this.totalQueue = proxyAutoSave.read();
        for(Map.Entry<String,HttpProxy> entry : totalQueue.entrySet()){
            this.idleQueue.add(entry.getValue());
        }

        //10分钟执行保存Proxy的操作
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                proxyAutoSave.save(totalQueue);
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    /**
     * 添加Proxy
     *
     * @param httpProxies
     */
    public void add(HttpProxy... httpProxies) {
        for (HttpProxy httpProxy : httpProxies) {
            if (totalQueue.containsKey(httpProxy.getKey())) {
                continue;
            }

            if (httpProxy.check()) {
                httpProxy.success();
                idleQueue.add(httpProxy);
                totalQueue.put(httpProxy.getKey(), httpProxy);
            }
        }
    }

    public void add(String address, int port) {
        this.add(new HttpProxy(address, port));
    }

    /**
     * 得到Proxy
     *
     * @return
     */
    public HttpProxy borrow() {
        HttpProxy httpProxy = null;
        try {
            Long time = System.currentTimeMillis();
            httpProxy = idleQueue.take();
            double costTime = (System.currentTimeMillis() - time) / 1000.0;
            _LOG.info("get proxy time >>>> " + costTime);

            HttpProxy p = totalQueue.get(httpProxy.getKey());
            p.borrow();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (httpProxy == null) {
            throw new NoSuchElementException();
        }
        return httpProxy;
    }

    /**
     * 反馈 Proxy
     *
     * @param httpProxy
     * @param httpStatus
     */
    public void reback(HttpProxy httpProxy, HttpStatus httpStatus) {
        switch (httpStatus) {
            case SC_OK:
                httpProxy.success();
                httpProxy.setReuseTimeInterval(HttpProxy.DEFAULT_REUSE_TIME_INTERVAL);
                break;
            case SC_FORBIDDEN:
                httpProxy.fail(httpStatus);
                // 被网站禁止，调节更长时间的访问频率
                httpProxy.setReuseTimeInterval(HttpProxy.DEFAULT_REUSE_TIME_INTERVAL * httpProxy.getFailedNum());
                _LOG.info(httpProxy.getProxy() + " >>>> reuseTimeInterval is >>>> " + TimeUnit.SECONDS.convert(httpProxy.getReuseTimeInterval(), TimeUnit.MILLISECONDS));
                break;
            default:
                httpProxy.fail(httpStatus);
                break;
        }

        // 失败超过 20 次，移除代理池队列
        if (httpProxy.getFailedNum() > 20) {
            httpProxy.setReuseTimeInterval(HttpProxy.FAIL_REVIVE_TIME_INTERVAL);
            _LOG.error("remove proxy >>>> " + httpProxy.getProxy() + ">>>>" + httpProxy.countErrorStatus() + " >>>> remain proxy >>>> " + idleQueue.size());
            return;
        }

        //失败超过 5次，10次，15次，检查本机与Proxy的连通性
        if (httpProxy.getFailedNum() > 0 && httpProxy.getFailedNum() % 5 == 0) {
            if (!httpProxy.check()) {
                httpProxy.setReuseTimeInterval(HttpProxy.FAIL_REVIVE_TIME_INTERVAL);
                _LOG.error("remove proxy >>>> " + httpProxy.getProxy() + ">>>>" + httpProxy.countErrorStatus() + " >>>> remain proxy >>>> " + idleQueue.size());
                return;
            }
        }
        try {
            idleQueue.put(httpProxy);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void allProxyStatus() {
        String re = "all proxy info >>>> \n";
        for (Map.Entry<String, HttpProxy> entry : totalQueue.entrySet()) {
            re += entry.getValue().toString() + "\n";
        }
        _LOG.info(re);
    }

    /**
     * 获取当前空闲的Proxy
     *
     * @return
     */
    public int getIdleNum() {
        return idleQueue.size();
    }
}
