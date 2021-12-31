package com.future94.gothrough.protocol.nio.handler.codec.serialization;

import com.future94.gothrough.protocol.nio.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * 对象编码器，配合{@link ObjectDecoder}使用
 * @author weilai
 */
@Slf4j
public class ObjectEncoder extends MessageToByteEncoder<Object> {

    @Override
    public byte[] encode(Object msg) throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = null;
        try {
            oout = new ObjectOutputStream(bout);
            oout.writeObject(msg);
            oout.flush();
        } finally {
            try {
                if (oout != null) {
                    oout.close();
                } else {
                    bout.close();
                }
            } catch (IOException ex) {
                log.error("ObjectDecoder Could not deserialize.", ex);
            }
        }
        return bout.toByteArray();
    }
}