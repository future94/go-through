package com.future94.gothrough.common.enums;

import lombok.Getter;

/**
 * @author weilai
 */
@Getter
public enum ResultEnum {

    /**
     *
     */
    SUCCESS(200, "成功"),

    INTERACTIVE_TYPE_NOT_FOUND(404, "交互类型不存在"),

    NO_SERVER_LISTEN(502, "不存在请求的监听接口"),

    FAIL(500, "失败"),

    ;
    private Integer code;

    private String desc;

    ResultEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
