package com.future94.gothrough.common.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * @author weilai
 */
@Getter
public enum InteractiveTypeEnum {

    /**
     *
     */
    UN_KNOW("未知"),

    COMMON_REPLY("通用回复标签"),

    HEART_BEAT("发送心跳"),

    SERVER_WAIT_CLIENT("需要客户端建立连接"),

    /**
     * 客户端启动像服务器请求建立连接
     */
    CLIENT_CONTROL("客户端请求建立连接"),

    /**
     * 服务端对{@link #CLIENT_CONTROL}类型的响应
     */
    CLIENT_CONTROL_ANSWER("服务端响应客户端请求建立连接"),

    /**
     * 客户端建立通道连接
     */
    CLIENT_CONNECT("客户端建立通道连接"),

    /**
     * 服务端对{@link #CLIENT_CONNECT}类型的响应
     */
    CLIENT_CONNECT_ANSWER("服务端响应客户端建立通道连接"),

    ;

    private String desc;

    InteractiveTypeEnum(String desc) {
        this.desc = desc;
    }

    public static InteractiveTypeEnum getEnumByName(String name) {
        if (Objects.isNull(name)) {
            return null;
        }
        for (InteractiveTypeEnum e : InteractiveTypeEnum.values()) {
            if (e.name().equals(name)) {
                return e;
            }
        }
        return null;
    }

}
