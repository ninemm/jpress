package io.jpress.module.crawler.proxy;

import com.jfinal.log.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Random;

/**
 * Proxy List对象
 *
 * @author Eric.Huang
 * @date 2019-05-25 23:57
 * @package io.jpress.module.crawler.proxy
 **/

public class Proxys extends ArrayList<Proxy> {

    private static final Log _LOG = Log.getLog(Proxys.class);

    public static Random random = new Random();

    public void add(String ip, int port) {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));
        this.add(proxy);
    }

    public void add(String proxyStr) throws Exception {
        try {
            String[] infos = proxyStr.split(":");
            String ip = infos[0];
            int port = Integer.valueOf(infos[1]);

            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));
            this.add(proxy);
        } catch (Exception ex) {
            _LOG.info("Exception", ex);
        }
    }

    public void addAllFromFile(File file) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        String line = null;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("#")||line.isEmpty()) {
                continue;
            } else {
                this.add(line);
            }
        }
    }

}
