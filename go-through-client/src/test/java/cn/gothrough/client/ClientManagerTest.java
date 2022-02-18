package cn.gothrough.client;

/**
 * @author weilai
 */
class ClientManagerTest {


    public static void main(String[] args) {
        start();
    }

    private static void start() {
        ClientManager serverManager = new ClientManager();
        serverManager.connect();
    }
}