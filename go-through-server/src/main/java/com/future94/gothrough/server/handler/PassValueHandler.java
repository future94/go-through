package com.future94.gothrough.server.handler;

import com.future94.gothrough.common.utils.Optional;
import com.future94.gothrough.protocol.channel.GoThroughSocketChannel;
import com.future94.gothrough.server.process.ProcessStatusDTO;

/**
 * @author weilai
 */
public interface PassValueHandler<R, W> {

    /**
     * @return 是否执行成功
     */
    ProcessStatusDTO process(GoThroughSocketChannel<R, W> goThroughSocketChannel, Optional<R> model) throws Exception;
}
