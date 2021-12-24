package com.future94.gothrough.examples.server;

import com.future94.gothrough.server.config.InteractiveServerConfig;
import com.future94.gothrough.server.listen.config.InteractiveServerListenConfig;
import com.future94.gothrough.server.listen.config.ServerListenConfig;
import com.future94.gothrough.server.listen.thread.ServerListenThreadManager;
import com.future94.gothrough.server.thread.ServerThreadManager;

import java.io.IOException;

/**
 * @author weilai
 */
public class ServerApplication {

    public static void main(String[] args) throws IOException {
        new ServerThreadManager(new InteractiveServerConfig(false)).start();

        ServerListenConfig serverListenConfig = new InteractiveServerListenConfig(14567, false);
        new ServerListenThreadManager(serverListenConfig);
    }
}
