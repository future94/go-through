package cn.gothrough.server.boss.handler;

import cn.gothrough.protocol.constants.AttributeKeyConstants;
import cn.gothrough.protocol.handler.MessageHandler;
import cn.gothrough.protocol.message.BinaryMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 处理客户端发送过来的{@link BinaryMessage#TYPE_TRANSFER}
 * @author weilai
 */
public class TransferMessageHandler implements MessageHandler {

    private static Logger logger = LoggerFactory.getLogger(TransferMessageHandler.class);

    private static final BlockingQueue<ByteBuf> waitingWrite = new LinkedBlockingQueue<>();

    public TransferMessageHandler() {
        Thread thread = new Thread();
        thread.start();
    }

    @Override
    public boolean supports(byte type) {
        return BinaryMessage.TYPE_TRANSFER == type;
    }

    /**
     * 将客户端转发过来的数据写入ListenChannel实现转发
     */
    @Override
    public boolean process(ChannelHandlerContext ctx, BinaryMessage message) {
        Channel clientChannel = ctx.channel();
        Channel listenChannel = clientChannel.attr(AttributeKeyConstants.SERVER_LISTEN_CHANNEL).get();
        if (listenChannel == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("Transfer消息未找到ListenChannel, serverListenId:[{}]", message.getData());
            }
            return false;
        }
        if (!listenChannel.isActive()) {
            if (logger.isWarnEnabled()) {
                logger.warn("Transfer消息找到的ListenChannel已经处于非活跃状态, serverListenId:[{}]", message.getData());
            }
            listenChannel.close();
            return false;
        }
        logger.debug("Transfer消息可写状态:[{}]", listenChannel.isWritable());
        int length = message.getByteBuffer().length;
        ByteBuf buffer = ctx.alloc().buffer(length);
        buffer.writeBytes(message.getByteBuffer());
        ChannelFuture channelFuture = listenChannel.writeAndFlush(buffer);
        try {
            logger.debug("Transfer消息写入处理{}, length:[{}]", channelFuture.sync().isSuccess(), length);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    static class WriteTask implements Runnable {

        private final Channel listenChannel;

        WriteTask(Channel listenChannel) {
            this.listenChannel = listenChannel;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    ByteBuf take = waitingWrite.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
