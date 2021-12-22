package com.future94.gothrough.server;

import com.future94.gothrough.server.config.InteractiveServerConfig;
import com.future94.gothrough.server.listen.config.InteractiveServerListenConfig;
import com.future94.gothrough.server.listen.config.ServerListenConfig;
import com.future94.gothrough.server.listen.thread.ServerListenThreadManager;
import com.future94.gothrough.server.thread.ServerThreadManager;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author weilai
 */
@Slf4j
public class ServerApplication {

    public static void main(String[] args) throws IOException {
        new ServerThreadManager(new InteractiveServerConfig()).start();

        ServerListenConfig serverListenConfig = new InteractiveServerListenConfig(8081);
        new ServerListenThreadManager(serverListenConfig);
    }
}
