package me.hupeng.java.monitorserver.Mina;

import org.apache.mina.core.session.IoSession;

/**
 * Created by HUPENG on 2016/9/6.
 */
public interface MinaSimpleListener {
    public void onReceive(Object obj, IoSession ioSession);

    /**
     * 某个客户机上线了
     * */
    public void onLine(int clientId);

    /**
     * 某个客户机下线了
     * */
    public void offLine(int clientId);
}
