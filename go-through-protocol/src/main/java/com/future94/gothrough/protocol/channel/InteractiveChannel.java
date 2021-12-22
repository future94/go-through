package com.future94.gothrough.protocol.channel;

import com.future94.gothrough.common.utils.ByteBufferUtils;
import com.future94.gothrough.protocol.model.InteractiveModel;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author weilai
 */
@Slf4j
public class InteractiveChannel implements GoThroughSocketChannel<InteractiveModel, InteractiveModel> {

    private ReentrantLock readLock = new ReentrantLock(false);

    private ReentrantLock writeLock = new ReentrantLock(false);

    private final Gson gson = new Gson();

    private byte[] lenBytes = new byte[4];

    private InputStream inputStream;

    private OutputStream outputStream;

    private Socket socket;

    private java.nio.channels.SocketChannel socketChannel;

    public InteractiveChannel(Socket socket) throws IOException {
        this.setSocket(socket);
    }

    @Override
    public InteractiveModel read() throws Exception {
        ReentrantLock readLock = this.readLock;
        byte[] lenBytes = this.lenBytes;
        readLock.lock();
        try {
            int offset = 0;

            InputStream is = getInputSteam();

            int len;
            for (; offset < lenBytes.length; ) {
                len = is.read(lenBytes, offset, lenBytes.length - offset);
                if (len < 0) {
                    // 如果-1，提前关闭了，又没有获得足够的数据，那么就抛出异常
                    throw new IOException("Insufficient byte length[" + lenBytes.length + "] when io closed");
                }
                offset += len;
            }

            int length = ByteBufferUtils.bytesToInt(lenBytes);

            offset = 0;
            byte[] b = new byte[length];
            for (; offset < length; ) {
                len = is.read(b, offset, length - offset);
                if (len < 0) {
                    // 如果-1，提前关闭了，又没有获得足够的数据，那么就抛出异常
                    throw new IOException("Insufficient byte length[" + length + "] when io closed");
                }
                offset += len;
            }
            return gson.fromJson(new String(b, StandardCharsets.UTF_8), InteractiveModel.class);
        } finally {
            readLock.unlock();
        }
    }

    private InputStream getInputSteam() throws IOException {
        InputStream inputStream;
        if ((inputStream = this.inputStream) == null) {
            inputStream = this.inputStream = this.socket.getInputStream();
        }
        return inputStream;
    }

    @Override
    public void write(InteractiveModel value) throws Exception {
        ReentrantLock writeLock = this.writeLock;
        writeLock.lock();
        try {
            byte[] writeValueByte = gson.toJson(value).getBytes(StandardCharsets.UTF_8);
            if (Objects.nonNull(this.socketChannel)) {
                int writeByte;
                writeByte = ByteBufferUtils.channelWrite(this.socketChannel, ByteBuffer.wrap(ByteBufferUtils.intToBytes(writeValueByte.length)));
                if (writeByte == 0) {
                    log.warn("写入管道数据为0字节");
                }
                writeByte = ByteBufferUtils.channelWrite(this.socketChannel, ByteBuffer.wrap(writeValueByte));
                if (writeByte == 0) {
                    log.warn("写入管道数据为0字节");
                }
            } else {
                OutputStream os = getOutputStream();
                os.write(ByteBufferUtils.intToBytes(writeValueByte.length));
                os.write(writeValueByte);
            }
        } finally {
            writeLock.unlock();
        }
    }

    private OutputStream getOutputStream() throws IOException {
        OutputStream outputStream;
        if ((outputStream = this.outputStream) == null) {
            outputStream = this.outputStream = this.getSocket().getOutputStream();
        }
        return outputStream;
    }

    @Override
    public void flush() throws Exception {
        ReentrantLock writeLock = this.writeLock;

        writeLock.lock();
        try {
            getOutputStream().flush();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void writeAndFlush(InteractiveModel value) throws Exception {
        ReentrantLock writeLock = this.writeLock;

        writeLock.lock();
        try {
            this.write(value);
            this.flush();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Socket getSocket() {
        return this.socket;
    }

    @Override
    public void setSocket(Socket socket) throws IOException {
        if (Objects.nonNull(this.socket)) {
            throw new UnsupportedOperationException("socket cannot be set repeatedly");
        }
        this.socket = socket;
        this.socketChannel = socket.getChannel();
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
    }

    @Override
    public void close() throws IOException {
        this.socket.close();
    }
}
