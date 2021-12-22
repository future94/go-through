package com.future94.gothrough.common.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * @author weilai
 */
public class SequenceUtils {

    public static String genSocketPartKey(Integer listenPort) {
        return String.join(":", "SPK", String.format("%05d", listenPort), LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")), String.valueOf(new Random().nextInt(9999)));
    }

    public static Integer getSocketPortByPartKey(String socketPartKey) {
        String[] split = socketPartKey.split(":");
        return Integer.valueOf(split[1]);
    }

    public static String genInteractiveSeq() {
        return String.join(":", "IS", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")), String.valueOf(new Random().nextInt(9999)));
    }
}
