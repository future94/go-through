package com.future94.gothrough.protocol.nio.handler;

/**
 * 继承{@link SimpleChannelReadableHandler}处理读到数据逻辑
 * @param <I> 实际拿到的类型  需要在{@link com.future94.gothrough.protocol.nio.server.IServer}中设置对应的
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

    @Override
    public void channelRead(Object msg) throws Exception {
        @SuppressWarnings("unchecked")
        I imsg = (I) msg;
        channelRead0(imsg);
    }

    protected abstract void channelRead0(I msg);
}