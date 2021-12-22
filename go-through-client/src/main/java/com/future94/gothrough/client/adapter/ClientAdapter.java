package com.future94.gothrough.client.adapter;

import com.future94.gothrough.protocol.channel.GoThroughSocketChannel;
import com.future94.gothrough.protocol.model.dto.ServerWaitClientDTO;

/**
 * @author weilai
 */
public interface ClientAdapter<R, W> {

    /**
     * 客户端请求建立连接
     */
    boolean createControlChannel() throws Exception;

    boolean createConnect(ServerWaitClientDTO dto);

    void waitMessage() throws Exception;

    void close() throws Exception;

    /**
     * 发送心跳测试
     */
    void sendHeartbeatTest() throws Exception;

    /**
     *
     */
    GoThroughSocketChannel<R, W> getGoThroughSocketChannel();
}
