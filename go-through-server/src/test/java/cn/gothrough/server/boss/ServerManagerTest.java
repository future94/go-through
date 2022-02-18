package cn.gothrough.server.boss;

import cn.gothrough.server.listen.ServerListenManager;

/**
 * @author weilai
 */
class ServerManagerTest {

    public static void main(String[] args) {
        startBoss();
        startlisten();
    }

    private static void startBoss() {
        ServerManager serverManager = new ServerManager();
        serverManager.start();
    }
    private static void startlisten() {
        ServerListenManager listenManager = new ServerListenManager();
        listenManager.start();
    }

}