package com.future94.gothrough.protocol.nio.handler;

import com.future94.gothrough.protocol.nio.handler.codec.Decoder;
import com.future94.gothrough.protocol.nio.handler.context.ChannelHandlerContext;

import java.lang.reflect.ParameterizedType;

/**
 * 继承{@link SimpleChannelReadableHandler}处理读到数据逻辑
 * @param <I> 实际拿到的类型  需要在{@link com.future94.gothrough.protocol.nio.thread.server.IServer}中设置对应的
 *                          编码器{@link com.future94.gothrough.protocol.nio.handler.codec.Encoder}和
 *                          解码器{@link com.future94.gothrough.protocol.nio.handler.codec.Decoder}
 *
 * <p>屏蔽掉了{@link ClassCastException}. 如果抛出{@link ClassCastException}, 认为该处理器不是这个数据的处理器.
 *
 * eg.
 * <p>
 *    public class TestChannelReadableHandler extends SimpleChannelReadableHandler<String> {
 *
 *      @Override
 *      protected void channelRead0(String msg) {
 *         System.out.println(msg);
 *      }
 *    }
 */
public abstract class SimpleChannelReadableHandler<I> implements ChannelReadableHandler {

    private I msg;

    @Override
    @SuppressWarnings("unchecked")
    public boolean supports(Decoder<?> decoder, byte[] payload) {
        boolean codec = false;
        Object decode = null;
        try {
            decode = decoder.decode(payload);
            codec = true;
        } catch (Exception ignored) {
        }
        boolean byteSupport = false;
        Class<I> genericType = (Class<I>)(((ParameterizedType) (getClass().getGenericSuperclass())).getActualTypeArguments()[0]);
        if (genericType.isAssignableFrom(byte[].class)) {
            byteSupport = true;
        }
        if ((codec && !byteSupport) || (!codec && byteSupport)) {
            try {
                this.msg = (I) decode;
                return support(msg);
            } catch (ClassCastException e) {
                return false;
            }
        }
        return false;
    }

    protected abstract boolean support(I msg);

    @Override
    public void channelRead(ChannelHandlerContext ctx) throws Exception {
        channelRead0(ctx, msg);
        msg = null;
    }

    protected abstract void channelRead0(ChannelHandlerContext ctx, I msg);
}