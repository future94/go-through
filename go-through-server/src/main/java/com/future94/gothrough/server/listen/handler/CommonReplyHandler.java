package com.future94.gothrough.server.listen.handler;

import com.future94.gothrough.common.enums.InteractiveTypeEnum;
import com.future94.gothrough.protocol.channel.GoThroughSocketChannel;
import com.future94.gothrough.protocol.model.InteractiveModel;

/**
 * @author weilai
 */
public class CommonReplyHandler implements ClientRecvHandler<InteractiveModel, InteractiveModel> {

    private static final CommonReplyHandler INSTANCE = new CommonReplyHandler();

    private CommonReplyHandler() {
    }

    public static CommonReplyHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean process(InteractiveModel model, GoThroughSocketChannel<InteractiveModel, InteractiveModel> channel) throws Exception {
        InteractiveTypeEnum interactiveTypeEnum = InteractiveTypeEnum.getEnumByName(model.getInteractiveType());
        if (!InteractiveTypeEnum.COMMON_REPLY.equals(interactiveTypeEnum)) {
            return false;
        }
        // TODO?
        return true;
    }
}
