package io.jpress.module.crawler.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * IP 工具类
 *
 * @author Eric.Huang
 * @date 2019-05-25 23:36
 * @package io.jpress.module.crawler.util
 **/

public class IpUtils {

    private final static Pattern IPV4_PATTERN = Pattern.compile("^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");

    private static class IpUtilsHolder {
        private static final InetAddress localAddr = new IpUtils().init();
    }

    private IpUtils() {
    }

    public static InetAddress getLocalAddr() {
        return IpUtilsHolder.localAddr;
    }

    private InetAddress init() {
        try {
            //直接获取IP地址，适应于Windows机器
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        //遍历所有的网络接口获取
        Enumeration<NetworkInterface> enumeration = null;
        try {
            enumeration = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        while (enumeration != null && enumeration.hasMoreElements()) {
            NetworkInterface networkInterface = enumeration.nextElement();
            Enumeration<InetAddress> addr = networkInterface.getInetAddresses();
            while (addr.hasMoreElements()) {
                InetAddress localAddr = addr.nextElement();

                if (localAddr.getHostAddress() != null && IPV4_PATTERN.matcher(localAddr.getHostAddress()).matches()) {
                    return localAddr;
                }
            }
        }
        return null;
    }
}
