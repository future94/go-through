package cn.gothrough.client;

import cn.gothrough.client.context.GoThroughContext;
import cn.gothrough.client.handler.boss.DispatcherHandler;
import cn.gothrough.client.handler.intranet.TransferHandler;
import cn.gothrough.protocol.codec.BinaryMessageDecoder;
import cn.gothrough.protocol.codec.BinaryMessageEncoder;
import cn.gothrough.protocol.constants.CodecConstants;
import cn.gothrough.protocol.handler.HeartbeatHandler;
import cn.gothrough.protocol.message.BinaryMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author weilai
 */
public class ClientManager {

    private static Logger logger = LoggerFactory.getLogger(ClientManager.class);

    private final Bootstrap serverBootstrap;

    private final Bootstrap intranetBootstrap;

    public ClientManager() {
        Bootstrap intranetBootstrap = new Bootstrap();
        intranetBootstrap.group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new TransferHandler());
                    }
                });
        this.intranetBootstrap = intranetBootstrap;
        Bootstrap serverBootstrap = new Bootstrap();
        serverBootstrap.group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new BinaryMessageEncoder());
                        ch.pipeline().addLast(new BinaryMessageDecoder(CodecConstants.MAX_FRAME_LENGTH, CodecConstants.LENGTH_FIELD_OFFSET, CodecConstants.LENGTH_FIELD_LENGTH, CodecConstants.LENGTH_ADJUSTMENT, CodecConstants.INITIAL_BYTES_TO_STRIP));
//                        ch.pipeline().addLast(new HeartbeatHandler(60, 50, 0));
                        ch.pipeline().addLast(new DispatcherHandler(serverBootstrap, intranetBootstrap));
                    }
                }
           );
        this.serverBootstrap = serverBootstrap;
    }

    public void connect() {
        serverBootstrap.connect("127.0.0.1", 9507).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                GoThroughContext.setBossServerChannel(future.channel());
                future.channel().writeAndFlush(BinaryMessage.buildAuthMessage("clientId"));
                logger.info("连接成功");
            } else {
                logger.warn("连接失败", future.cause());
                reconnect();
            }
        });
    }

    public void reconnect() {
        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        connect();
    }
}
