package io.jpress.module.crawler.proxy;

import com.jfinal.log.Log;
import com.jfinal.weixin.sdk.kit.IpKit;
import io.jpress.module.crawler.util.HttpStatus;
import io.jpress.module.crawler.util.IpUtils;
import io.jpress.module.crawler.util.ProxyPersistUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Http 代理
 *
 * @author Eric.Huang
 * @date 2019-05-25 23:24
 * @package io.jpress.module.crawler.proxy
 **/

public class HttpProxy implements Delayed {

    private static final Log _LOG = Log.getLog(HttpProxy.class);

    /** 从一次请求结束到再次可以请求的默认时间间隔(ms) */
    public final static int DEFAULT_REUSE_TIME_INTERVAL = 1500;
    /** 请求失败，重试的时间间隔(ms) */
    public final static int FAIL_REVIVE_TIME_INTERVAL = 2 * 60 * 60 * 1000;

    private int failedNum = 0;
    private int borrowNum = 0;
    private Proxy proxy;
    private InetAddress localAddr;

    private int reuseTimeInterval = 0;
    /** 当前Proxy可重用的时间,纳秒 */
    private Long canReuseTime = 0L;
    private Map<HttpStatus, Integer> countErrorStatus = new HashMap<HttpStatus, Integer>();

    public HttpProxy(String address, int port) {
        this(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(address, port)), 0, 0, DEFAULT_REUSE_TIME_INTERVAL);
    }

    public HttpProxy(Proxy proxy) {
        this(proxy, 0, 0, DEFAULT_REUSE_TIME_INTERVAL);
    }

    public HttpProxy(Proxy proxy, int borrowNum, int failedNum, int reuseTimeInterval) {
        // 获取当前机器的ip地址

        this.localAddr = IpUtils.getLocalAddr();
        if (localAddr == null) {
            _LOG.error("cannot get local IP!");
            System.exit(0);
        }
        this.proxy = proxy;
        this.canReuseTime = System.nanoTime() + TimeUnit.NANOSECONDS.convert(reuseTimeInterval, TimeUnit.MILLISECONDS);
    }

    /**
     * 检查本地机器和Proxy之间的连通性
     *
     * @return
     */
    public boolean check() {
        boolean isReachable = false;
        Socket socket = null;
        try {
            socket = new Socket();
            socket.bind(new InetSocketAddress(localAddr, 0));
            socket.connect(proxy.address(), 3000);
            isReachable = true;
        } catch (Exception e) {
            _LOG.error("bad proxy >>>" + this.proxy.toString());
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return isReachable;
    }

    public void success() {
        //将 failNum 清零
        this.failedNum = 0;
        countErrorStatus.clear();
    }

    /**
     * 代理失败，记录相应的错误状态码
     *
     * @param httpStatus
     */
    public void fail(HttpStatus httpStatus) {
        if (countErrorStatus.containsKey(httpStatus)) {
            countErrorStatus.put(httpStatus, countErrorStatus.get(httpStatus) + 1);
        } else {
            countErrorStatus.put(httpStatus, 1);
        }
        this.failedNum++;
    }

    /**
     * 输出错误请求的日志
     *
     * @return
     */
    public String countErrorStatus() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<HttpStatus, Integer> entry : countErrorStatus.entrySet()) {
            stringBuilder.append(entry.getKey().name()).append("_").append(entry.getKey().getCode()).append("->").append(entry.getValue());
        }
        return stringBuilder.toString();
    }

    /**
     * 对请求进行计数
     */
    public void borrow() {
        this.borrowNum++;
    }

    public int getFailedNum() {
        return failedNum;
    }

    public int getBorrowNum() {
        return borrowNum;
    }

    public void setReuseTimeInterval(int reuseTimeInterval) {
        this.reuseTimeInterval = reuseTimeInterval;
        this.canReuseTime = System.nanoTime() + TimeUnit.NANOSECONDS.convert(reuseTimeInterval, TimeUnit.MILLISECONDS);
    }

    public int getReuseTimeInterval() {
        return this.reuseTimeInterval;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }


    public String getKey() {
        InetSocketAddress address = (InetSocketAddress) proxy.address();
        return address.getAddress().getHostAddress() + ":" + address.getPort();
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(canReuseTime - System.nanoTime(), TimeUnit.NANOSECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        HttpProxy that = (HttpProxy) o;
        return canReuseTime > that.canReuseTime ? 1 : (canReuseTime < that.canReuseTime ? -1 : 0);
    }

    @Override
    public String toString() {
        return this.proxy.toString()
                + ">>> 使用:" + borrowNum + "次 "
                + ">>> 连续失败:" + failedNum + "次"
                + ">>> 距离下次可用:" + TimeUnit.MILLISECONDS.convert(canReuseTime > System.nanoTime() ? canReuseTime - System.nanoTime() : 0, TimeUnit.NANOSECONDS) + " ms后";
    }
}
