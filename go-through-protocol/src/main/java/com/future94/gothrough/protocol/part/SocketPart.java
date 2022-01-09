package com.future94.gothrough.protocol.part;

import java.nio.channels.SocketChannel;

/**
 * 相互交互的Socket组件
 * @author weilai
 */
public interface SocketPart {

    String getSocketPartKey();

    void setSocketPartKey(String socketPartKey);

    SocketChannel getRecvSocket();

    void setRecvSocket(SocketChannel recvSocket);

    SocketChannel getSendSocket();

    void setSendSocket(SocketChannel sendSocket);

    void cancel();

    boolean createPassWay();

    boolean isValid();
}
