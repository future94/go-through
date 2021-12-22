package com.future94.gothrough.server.service.impl;

import com.future94.gothrough.protocol.channel.InteractiveChannel;
import com.future94.gothrough.protocol.channel.GoThroughSocketChannel;
import com.future94.gothrough.protocol.model.InteractiveModel;
import com.future94.gothrough.server.adapter.ClientSocketAdapter;
import com.future94.gothrough.server.adapter.ReadAheadPassValueAdapter;
import com.future94.gothrough.server.service.ServerService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;

/**
 * @author weilai
 */
@Slf4j
public class InteractiveServerService implements ServerService<InteractiveModel, InteractiveModel> {

    @Override
    public ClientSocketAdapter createClientSocketAdapter() {
        return new ReadAheadPassValueAdapter(this);
    }

    @Override
    public GoThroughSocketChannel<InteractiveModel, InteractiveModel> createSocketChannel(Socket socket) throws IOException {
        return new InteractiveChannel(socket);
    }
}
