package io.jpress.commons.utils;

import com.jfinal.log.Log;
import io.undertow.util.CopyOnWriteMap;

import javax.websocket.Session;
import java.io.IOException;

/**
 * websocket 工具类
 *
 * @author Eric.Huang
 * @date 2019-04-18 19:13
 * @package io.jboot.admin.socket
 **/

public class WebSocketUtil {

    private static final Log LOG = Log.getLog(WebSocketUtil.class);
    private static CopyOnWriteMap<String, Session> sessions = new CopyOnWriteMap<>();

    /**
     * 向连接池中添加连接
     * @param schedulerId
     * @param session
     */
    public static void addMessageInbound(String schedulerId, Session session) {
        LOG.info("new schedulerId : " + schedulerId + " join. getMaxIdleTimeout: " + session.getMaxIdleTimeout());
        sessions.put(schedulerId, session);
    }

    /**
     * 从连接池中移除连接
     * @param schedulerId
     */
    public static void removeMessageInbound(String schedulerId) {
        LOG.info("then schedulerId : " + schedulerId + " exit. ");
        Session session = sessions.get(schedulerId);
        if (session != null && session.isOpen()) {
            sessions.remove(schedulerId);
            quietClose(session);
        }
    }

    /**
     * 从连接池中移除连接
     * @param schedulerId
     */
    public static void removeMessageInbound(Session session) {
        if (session != null && session.isOpen()) {
            sessions.remove(session);
            quietClose(session);
        }
    }

    /**
     * 向用户发送数据
     * @param schedulerId
     * @param message
     */
    public static void sendMessage(String schedulerId, String message) {
        try {
            Session session = sessions.get(schedulerId);
            if (session != null && session.isOpen()) {
                session.getAsyncRemote().setSendTimeout(5000);
                session.getAsyncRemote().sendText(message);
                session.getAsyncRemote().flushBatch();
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public static void quietClose(Session session) {
        try {
            session.close();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
