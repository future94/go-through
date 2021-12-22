package com.future94.gothrough.server.listen.handler;

import com.future94.gothrough.protocol.channel.GoThroughSocketChannel;

/**
 * @author weilai
 */
public interface ClientRecvHandler<R, W> {

    boolean process(R model, GoThroughSocketChannel<R, W> channel) throws Exception;
}
