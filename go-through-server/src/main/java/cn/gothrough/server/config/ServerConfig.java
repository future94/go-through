package cn.gothrough.server.config;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author weilai
 */
public class ServerConfig {

    private static final ServerConfig INSTANCE = new ServerConfig();

    /**
     * 服务端监听端口与要代理的内网地址之间的关系
     */
    private final Map<Integer, ClientProxyConfig> listenPortClientInfoCache = new ConcurrentHashMap<>();

    /**
     * 为客户端key分配的代理转发端口映射关系
     */
    private final Map<String, List<Integer>> clientKeyProxyPortCache = new ConcurrentHashMap<>();

    private List<Client> clientList;

    private ServerConfig() {
        clientKeyProxyPortCache.put("clientId", Collections.singletonList(12345));
        ClientProxyConfig proxyConfig = new ClientProxyConfig();
        proxyConfig.setProxyName("gothrough");
        proxyConfig.setIntranetHost("127.0.0.1");
        proxyConfig.setIntranetPort(8080);
        proxyConfig.setServerPort(12345);

        listenPortClientInfoCache.put(12345, proxyConfig);
    }

    public static ServerConfig getInstance() {
        return INSTANCE;
    }

    public ClientProxyConfig getClientProxyConfig(Integer listenPort) {
        return listenPortClientInfoCache.get(listenPort);
    }

    public List<Integer> proxyPortList(String key) {
        return clientKeyProxyPortCache.get(key);
    }

    public static class Client {

        /**
         * 客户端名称
         */
        private String clientName;

        /**
         * 转发映射关系
         */
        private List<ClientProxyConfig> proxyConfig;

        public String getClientName() {
            return clientName;
        }

        public void setClientName(String clientName) {
            this.clientName = clientName;
        }

        public List<ClientProxyConfig> getProxyConfig() {
            return proxyConfig;
        }

        public void setProxyConfig(List<ClientProxyConfig> proxyConfig) {
            this.proxyConfig = proxyConfig;
        }
    }

    public static class ClientProxyConfig {

        /**
         * 转发对名字
         */
        private String proxyName;

        /**
         * 要暴露的内网主机地址
         */
        private String intranetHost;

        /**
         * 要暴露的内网端口
         */
        private Integer intranetPort;

        /**
         * 对应的服务端地址
         */
        private Integer serverPort;

        public String getProxyName() {
            return proxyName;
        }

        public void setProxyName(String proxyName) {
            this.proxyName = proxyName;
        }

        public String getIntranetHost() {
            return intranetHost;
        }

        public void setIntranetHost(String intranetHost) {
            this.intranetHost = intranetHost;
        }

        public Integer getIntranetPort() {
            return intranetPort;
        }

        public void setIntranetPort(Integer intranetPort) {
            this.intranetPort = intranetPort;
        }

        public Integer getServerPort() {
            return serverPort;
        }

        public void setServerPort(Integer serverPort) {
            this.serverPort = serverPort;
        }

        public String getHostName() {
            return this.intranetHost + ":" + this.getIntranetPort();
        }
    }
}
