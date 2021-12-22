package com.future94.gothrough.client.handler;

import com.future94.gothrough.client.adapter.ClientAdapter;

/**
 * @author weilai
 */
public interface ClientHandler<R, W> {

    /**
     * 处理消息
     *
     * @param model         读取到的消息
     * @param clientAdapter 客户端适配器
     * @return true找到对应消息类型并处理成功
     * false不是对应消息类型跳过不做处理
     * @throws Exception 处理失败
     */
    boolean process(R model, ClientAdapter<R, W> clientAdapter) throws Exception;
}
