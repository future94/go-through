package com.future94.gothrough.protocol.nio.handler;

import com.future94.gothrough.protocol.nio.handler.codec.serialization.ObjectDecoder;
import com.future94.gothrough.protocol.nio.handler.codec.serialization.ObjectEncoder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author weilai
 */
public class TestHandler {

    @Test
    public void cast() throws Exception {
        TestByteChannelReadableHandler byteHandler = new TestByteChannelReadableHandler();
        TestPrintChannelReadableHandler objectHandler = new TestPrintChannelReadableHandler();
        Assertions.assertTrue(byteHandler.supports(new ObjectDecoder(), new byte[1]));
        Assertions.assertFalse(objectHandler.supports(new ObjectDecoder(), new byte[1]));
        Assertions.assertFalse(byteHandler.supports(new ObjectDecoder(), new ObjectEncoder().encoder("123")));
        Assertions.assertTrue(objectHandler.supports(new ObjectDecoder(), new ObjectEncoder().encoder("123")));
        Assertions.assertTrue(byteHandler.supports(new ObjectDecoder(), "312".getBytes()));
        Assertions.assertFalse(objectHandler.supports(new ObjectDecoder(), "312".getBytes()));
    }
}
