package cn.gothrough.server.boss;

import cn.gothrough.protocol.codec.BinaryMessageDecoder;
import cn.gothrough.protocol.codec.BinaryMessageEncoder;
import cn.gothrough.protocol.constants.CodecConstants;
import cn.gothrough.protocol.handler.HeartbeatHandler;
import cn.gothrough.server.boss.handler.DispatcherHandler;
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
public class ServerManager {

    private static Logger logger = LoggerFactory.getLogger(ServerManager.class);

    private final NioEventLoopGroup bossGroup;

    private final NioEventLoopGroup workerGroup;


    public ServerManager() {
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();
    }

    public void start() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new BinaryMessageEncoder());
                        socketChannel.pipeline().addLast(new BinaryMessageDecoder(CodecConstants.MAX_FRAME_LENGTH, CodecConstants.LENGTH_FIELD_OFFSET, CodecConstants.LENGTH_FIELD_LENGTH, CodecConstants.LENGTH_ADJUSTMENT, CodecConstants.INITIAL_BYTES_TO_STRIP));
//                        socketChannel.pipeline().addLast(new HeartbeatHandler(60, 50, 0));
                        socketChannel.pipeline().addLast(new DispatcherHandler());
                    }
                })
                .option ( ChannelOption.SO_BACKLOG, 1024 )
                .option ( ChannelOption.SO_REUSEADDR, true );
        serverBootstrap.bind("127.0.0.1", 9507).addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()) {
                logger.info("启动成功");
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    bossGroup.shutdownGracefully();
                    workerGroup.shutdownGracefully();
                }));
            } else {
                logger.error("启动失败", channelFuture.cause());
            }
        });
    }
}
