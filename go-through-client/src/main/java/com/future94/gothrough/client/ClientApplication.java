package com.future94.gothrough.client;

import com.future94.gothrough.client.config.InteractiveClientConfig;
import com.future94.gothrough.client.thread.ClientThreadManager;

/**
 * @author weilai
 */
public class ClientApplication {

    public static void main(String[] args) throws Exception {
        InteractiveClientConfig config = new InteractiveClientConfig();
        config.setServerIp("192.168.10.48");
        config.setServerPort(19507);
        config.setServerExposedListenPort(8081);
        config.setExposedIntranetIp("127.0.0.1");
        config.setExposedIntranetPort(8763);
        new ClientThreadManager(config).start();
    }
}
