package cn.gothrough.protocol.message;

/**
 * 客户端与服务端传输数据的二进制消息
 * @author weilai
 */
public class BinaryMessage {

    /**
     * 头大小
     */
    public static final int HEAD_SIZE = 4;

    /**
     * 类型大小
     */
    public static final int TYPE_SIZE = 1;

    /**
     * 序号大小
     */
    public static final int SEQUENCE_SIZE = 8;

    /**
     * 携带数据大小
     */
    public static final int DATA_SIZE = 4;

    /**
     * 固定大小（不包括头）
     */
    public static final int FIXED_SIZE = TYPE_SIZE + SEQUENCE_SIZE + DATA_SIZE;

    /**
     * 心跳
     */
    public static final byte TYPE_HEARTBEAT = 0x01;

    /**
     * 授权
     */
    public static final byte TYPE_AUTH = 0x02;

    /**
     * 连接
     */
    public static final byte TYPE_CONNECT = 0x03;

    /**
     * 断开连接
     */
    public static final byte TYPE_DISCONNECT = 0x04;

    /**
     * 转发消息
     */
    public static final byte TYPE_TRANSFER = 0x05;

    /**
     * 消息类型
     */
    private byte type;

    /**
     * 消息序号
     */
    private long sequence;

    /**
     * 消息携带数据
     */
    private String data;

    /**
     * 数据
     */
    private byte[] byteBuffer;

    public static BinaryMessage buildHeartbeatMessage() {
        BinaryMessage message = new BinaryMessage();
        message.setType(TYPE_HEARTBEAT);
        return message;
    }

    public static BinaryMessage buildAuthMessage(String data) {
        BinaryMessage message = new BinaryMessage();
        message.setType(TYPE_AUTH);
        message.setData(data);
        return message;
    }

    public static BinaryMessage buildTransferMessage(String data, byte[] byteBuffer) {
        BinaryMessage message = new BinaryMessage();
        message.setType(TYPE_TRANSFER);
        message.setData(data);
        message.setByteBuffer(byteBuffer);
        return message;
    }

    public static BinaryMessage buildConnectMessage(String data, byte[] byteBuffer) {
        BinaryMessage message = new BinaryMessage();
        message.setType(TYPE_CONNECT);
        message.setData(data);
        message.setByteBuffer(byteBuffer);
        return message;
    }

    public static BinaryMessage buildDisconnectMessage(String data) {
        BinaryMessage message = new BinaryMessage();
        message.setType(TYPE_DISCONNECT);
        message.setData(data);
        return message;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public long getSequence() {
        return sequence;
    }

    public void setSequence(long sequence) {
        this.sequence = sequence;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public byte[] getByteBuffer() {
        return byteBuffer;
    }

    public void setByteBuffer(byte[] byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    @Override
    public String toString() {
        return "BinaryMessage{" +
                "type=" + type +
                ", sequence=" + sequence +
                ", data='" + data + '\'' +
                '}';
    }
}
