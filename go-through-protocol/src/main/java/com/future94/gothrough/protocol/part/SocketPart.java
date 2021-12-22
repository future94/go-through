package com.future94.gothrough.protocol.part;

import java.net.Socket;

/**
 * @author weilai
 */
public interface SocketPart {

    String getSocketPartKey();

    Socket getRecvSocket();

    Socket getSendSocket();

    void setSocketPartKey(String socketPartKey);

    void setRecvSocket(Socket recvSocket);

    void setSendSocket(Socket sendSocket);

    void cancel();

    boolean createPassWay();

    boolean isValid();
}
