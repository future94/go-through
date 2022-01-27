package com.future94.gothrough.protocol.model.dto;

import com.future94.gothrough.common.enums.ResultEnum;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author weilai
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InteractiveResultDTO {

    private Integer code;

    private String message;

    private Object data;

    public boolean isSuccess() {
        return ResultEnum.SUCCESS.getCode().equals(this.code);
    }

    public static InteractiveResultDTO buildSuccess() {
        return new InteractiveResultDTO(ResultEnum.SUCCESS.getCode(), ResultEnum.SUCCESS.getDesc(), null);
    }

    public static InteractiveResultDTO buildSuccess(Object data) {
        return new InteractiveResultDTO(ResultEnum.SUCCESS.getCode(), ResultEnum.SUCCESS.getDesc(), data);
    }

    public static InteractiveResultDTO buildFail() {
        return new InteractiveResultDTO(ResultEnum.FAIL.getCode(), ResultEnum.FAIL.getDesc(), null);
    }

    public static InteractiveResultDTO buildInteractiveTypeNotFound() {
        return new InteractiveResultDTO(ResultEnum.INTERACTIVE_TYPE_NOT_FOUND.getCode(), ResultEnum.INTERACTIVE_TYPE_NOT_FOUND.getDesc(), null);
    }

    public static InteractiveResultDTO buildNoServerListen() {
        return new InteractiveResultDTO(ResultEnum.NO_SERVER_LISTEN.getCode(), ResultEnum.NO_SERVER_LISTEN.getDesc(), null);
    }

    public <T> T covert(Class<T> dataType) {
        Gson gson = new Gson();
        return gson.fromJson(gson.toJson(data), dataType);
    }

}
