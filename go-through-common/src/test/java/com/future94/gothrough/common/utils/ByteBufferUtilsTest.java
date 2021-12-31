package com.future94.gothrough.common.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * @author weilai
 */
class ByteBufferUtilsTest {

    @Test
    void bytesToInt() {
        int source = 129;
        byte[] bytes = ByteBufferUtils.intToBytes(source);
        int toInt = ByteBufferUtils.bytesToInt(bytes);
        System.out.println(Arrays.toString(bytes));
        Assertions.assertEquals(source, toInt);
    }

    @Test
    void intToBytes() {
        int source = 129;
        byte[] bytes = ByteBufferUtils.intToBytes(source);
        System.out.println(Arrays.toString(bytes));
    }
}