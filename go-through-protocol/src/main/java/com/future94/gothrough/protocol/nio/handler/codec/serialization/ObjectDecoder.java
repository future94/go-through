package com.future94.gothrough.protocol.nio.handler.codec.serialization;

import com.future94.gothrough.protocol.nio.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 *  * 对象解码器，配合{@link ObjectEncoder}使用
 * @author weilai
 */
@Slf4j
public class ObjectDecoder extends ByteToMessageDecoder<Object> {

    @Override
    protected Object decode(byte[] payload) throws Exception {
        ByteArrayInputStream byteArrayInputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            byteArrayInputStream = new ByteArrayInputStream(payload);
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            return objectInputStream.readObject();
        } finally {
            try {
                if (byteArrayInputStream != null) {
                    byteArrayInputStream.close();
                }
                if (objectInputStream != null) {
                    objectInputStream.close();
                }
            } catch (IOException ex) {
                log.error("ObjectDecoder Could not serialize.", ex);
            }
        }
    }
}
