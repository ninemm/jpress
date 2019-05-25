package io.jpress.module.crawler.util;

import com.jfinal.log.Log;
import io.jpress.module.crawler.proxy.HttpProxy;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 持久化Proxy记录
 *
 * @author Eric.Huang
 * @date 2019-05-25 23:20
 * @package io.jpress.module.crawler.util
 **/

public class ProxyPersistUtils {

    private static final Log _LOG = Log.getLog(ProxyPersistUtils.class);

    /** 保存proxy的路径 */
    private File path;

    public ProxyPersistUtils() {
        this.path = getFile();
    }

    public Map<String, HttpProxy> read() {
        Map<String, HttpProxy> map = new ConcurrentHashMap<String, HttpProxy>();
        String content = null;
        try {
            content = FileUtils.readFileToString(this.path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (content != null && content.length() > 0) {
            String[] lines = content.split("\n");
            for (String line : lines) {
                String[] tmps = line.split(":");
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(tmps[0], Integer.parseInt(tmps[1])));
                HttpProxy httpProxy = new HttpProxy(proxy,
                        Integer.parseInt(tmps[2]),
                        Integer.parseInt(tmps[3]),
                        Integer.parseInt(tmps[4]));
                map.put(httpProxy.getKey(), httpProxy);
            }
        }
        return map;
    }

    public void save(Map<String, HttpProxy> totalQueue) {
        if (totalQueue.size() == 0) {
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, HttpProxy> entry : totalQueue.entrySet()) {
            InetSocketAddress address = (InetSocketAddress) entry.getValue().getProxy().address();
            stringBuilder.append(address.getAddress().getHostAddress()).append(":")
                    .append(address.getPort()).append(":")
                    .append(entry.getValue().getBorrowNum()).append(":")
                    .append(entry.getValue().getFailedNum()).append(":")
                    .append(entry.getValue().getReuseTimeInterval()).append("\n");
        }
        try {
            FileUtils.writeStringToFile(this.path, stringBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getFile() {
        File file = new File(System.getProperty("java.io.tmpdir") + File.separator + "proxy.tmp");
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    throw new IOException(file + "create error");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

}
