package com.future94.gothrough.protocol.thread;

import com.future94.gothrough.protocol.part.BaseSocketPart;

/**
 * @author weilai
 */
public interface ThreadManager {

    /**
     * {@link BaseSocketPart#getRecvSocket()} 或者 {@link BaseSocketPart#getSendSocket()} 停止时通知 一次
     * 当通知两次时，表示{@link BaseSocketPart}中outToInPassWay和inToOutPassWay隧道中的Socket都停止了，需要停止SocketPart
     */
    default void noticeStopPassWay() {

    }

    default Boolean getNio() { return true;};
}
