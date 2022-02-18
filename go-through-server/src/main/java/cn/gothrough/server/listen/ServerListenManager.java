package cn.gothrough.server.listen;

import cn.gothrough.server.listen.handler.TransferHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author weilai
 */
public class ServerListenManager {

    private static Logger logger = LoggerFactory.getLogger(ServerListenManager.class);

    private final NioEventLoopGroup listenBossGroup;

    private final NioEventLoopGroup listenWorkerGroup;

    public ServerListenManager() {
        this.listenBossGroup = new NioEventLoopGroup();
        this.listenWorkerGroup = new NioEventLoopGroup();
    }

    public ServerListenManager(NioEventLoopGroup listenBossGroup, NioEventLoopGroup listenWorkerGroup) {
        this.listenBossGroup = listenBossGroup;
        this.listenWorkerGroup = listenWorkerGroup;
    }

    public void start() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(listenBossGroup, listenWorkerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new TransferHandler());
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_REUSEADDR, true);
        serverBootstrap.bind(12345).addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()) {
                logger.info("启动成功");
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    listenBossGroup.shutdownGracefully();
                    listenWorkerGroup.shutdownGracefully();
                }));
            } else {
                logger.error("启动失败", channelFuture.cause());
            }
        });

    }
}
