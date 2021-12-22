package com.future94.gothrough.server.process;

import com.future94.gothrough.protocol.channel.GoThroughSocketChannel;
import com.future94.gothrough.protocol.model.InteractiveModel;

/**
 * @author weilai
 */
public interface Process {

    boolean isProcess(InteractiveModel value);

    boolean run(GoThroughSocketChannel<InteractiveModel, InteractiveModel> goThroughSocketChannel, InteractiveModel value) throws Exception;
}
