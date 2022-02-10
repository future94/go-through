package com.future94.gothrough.server;

import com.future94.gothrough.server.boss.config.InteractiveServerConfig;
import com.future94.gothrough.server.listen.config.InteractiveServerListenConfig;
import com.future94.gothrough.server.listen.thread.ServerListenThreadManager;
import com.future94.gothrough.server.boss.thread.ServerThreadManager;

import java.io.IOException;

/**
 * @author weilai
 */
class ServerThreadTest {

    public static void main(String[] args) throws IOException {
        InteractiveServerConfig serverConfig = new InteractiveServerConfig();
        serverConfig.setServerPort(10000);
        ServerThreadManager serverThreadManager = new ServerThreadManager(serverConfig);
        serverThreadManager.start();


        InteractiveServerListenConfig listenConfig = new InteractiveServerListenConfig(12345);
        ServerListenThreadManager serverListenThreadManager = new ServerListenThreadManager(listenConfig);
    }
}