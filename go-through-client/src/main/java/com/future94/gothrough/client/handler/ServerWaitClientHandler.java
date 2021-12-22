package com.future94.gothrough.client.handler;

import com.future94.gothrough.client.adapter.ClientAdapter;
import com.future94.gothrough.common.enums.InteractiveTypeEnum;
import com.future94.gothrough.protocol.model.InteractiveModel;
import com.future94.gothrough.protocol.model.dto.ServerWaitClientDTO;

/**
 * @author weilai
 */
public class ServerWaitClientHandler implements ClientHandler<InteractiveModel, InteractiveModel> {

    private static final ServerWaitClientHandler INSTANCE = new ServerWaitClientHandler();

    private ServerWaitClientHandler() {
    }

    public static ServerWaitClientHandler getInstance() {
        return INSTANCE;
    }


    @Override
    public boolean process(InteractiveModel model, ClientAdapter<InteractiveModel, InteractiveModel> clientAdapter) throws Exception {
        InteractiveTypeEnum interactiveTypeEnum = InteractiveTypeEnum.getEnumByName(model.getInteractiveType());
        if (!InteractiveTypeEnum.SERVER_WAIT_CLIENT.equals(interactiveTypeEnum)) {
            return false;
        }
        ServerWaitClientDTO dto = model.getData().convert(ServerWaitClientDTO.class);
        clientAdapter.createConnect(dto);
        return true;
    }
}
