package com.future94.gothrough.protocol.nio.thread;

import com.future94.gothrough.protocol.nio.handler.ChannelReadableHandler;
import com.future94.gothrough.protocol.nio.handler.codec.Decoder;
import com.future94.gothrough.protocol.nio.handler.codec.Encoder;
import com.future94.gothrough.protocol.nio.handler.codec.serialization.ObjectDecoder;
import com.future94.gothrough.protocol.nio.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author weilai
 */
@Slf4j
public abstract class AbstractProcess implements IProcess{

    protected Encoder encoder = new ObjectEncoder();

    protected Decoder<?> decoder = new ObjectDecoder();

    /**
     * 当{@link java.nio.channels.SelectionKey#OP_READ}事件的回调
     */
    protected List<ChannelReadableHandler> channelReadableHandlers = new ArrayList<>();

    @Override
    public Object readBlockSocketChannel(SocketChannel socketChannel) throws Exception {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        socketChannel.read(byteBuffer);
        int frameSize = byteBuffer.getInt(0);
        byteBuffer = ByteBuffer.allocate(frameSize);
        socketChannel.read(byteBuffer);
        return this.getDecoder().decode(byteBuffer.array());
    }

    @Override
    public Encoder getEncoder() {
        return this.encoder;
    }

    @Override
    public void setEncoder(Encoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public Decoder<?> getDecoder() {
        return this.decoder;
    }

    @Override
    public void setDecoder(Decoder<?> decoder) {
        this.decoder = decoder;
    }

    @Override
    public void setReadableHandler(ChannelReadableHandler channelReadableHandler) {
        this.channelReadableHandlers.add(channelReadableHandler);
    }

    @Override
    public List<ChannelReadableHandler> getChannelReadableHandlers() {
        return this.channelReadableHandlers;
    }
}
