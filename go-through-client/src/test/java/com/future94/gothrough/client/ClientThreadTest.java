package com.future94.gothrough.client;


import com.future94.gothrough.client.config.InteractiveClientConfig;
import com.future94.gothrough.client.thread.ClientThreadManager;

/**
 * @author weilai
 */
class ClientThreadTest {

    public static void main(String[] args) throws Exception {
        InteractiveClientConfig config = new InteractiveClientConfig();
        config.setServerIp("127.0.0.1");
        config.setServerPort(10000);
        config.setServerExposedListenPort(12345);
        config.setExposedIntranetIp("127.0.0.1");
        config.setExposedIntranetPort(8080);
        ClientThreadManager threadManager = new ClientThreadManager(config);
        threadManager.start();
    }
}