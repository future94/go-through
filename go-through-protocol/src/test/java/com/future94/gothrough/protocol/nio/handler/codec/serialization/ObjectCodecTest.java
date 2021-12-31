package com.future94.gothrough.protocol.nio.handler.codec.serialization;

import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

/**
 * @author weilai
 */
class ObjectCodecTest {

    @Data
    static class ObjectTest implements Serializable {
        private String name;
        private int age;

        public ObjectTest(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    private ObjectTest objectTest;

    @BeforeEach
    public void beforeAll() {
        this.objectTest = new ObjectTest("weilai", 18);
    }

    @Test
    public void objectEncoder() throws Exception {
        ObjectEncoder objectEncoder = new ObjectEncoder();
        byte[] encode = objectEncoder.encode(objectTest);
        ObjectDecoder objectDecoder = new ObjectDecoder();
        Object decode = objectDecoder.decode(encode);
        System.out.println(decode);
    }

}