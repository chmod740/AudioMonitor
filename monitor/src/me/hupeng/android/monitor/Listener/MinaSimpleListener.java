package me.hupeng.android.monitor.Listener;

import org.apache.mina.core.session.IoSession;

/**
 * Created by HUPENG on 2016/9/6.
 */
public interface MinaSimpleListener {
    public void onReceive(Object obj, IoSession ioSession);


    /**
     * socket连接建立
     * */
    public void opened();

    /**
     * socket连接关闭
     * */
    public void closed();
}
