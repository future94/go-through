package com.future94.gothrough.protocol.model;

import com.future94.gothrough.common.enums.InteractiveTypeEnum;
import com.future94.gothrough.common.utils.SequenceUtils;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author weilai
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InteractiveModel implements Serializable {

    /**
     * 交互序列，用于异步通信
     */
    private String interactiveSeq;

    /**
     * 交互类型
     */
    private String interactiveType;

    /**
     * 交互实体内容(json)
     */
    private Data data;

    public static InteractiveModel of(String interactiveSeq, InteractiveTypeEnum interactiveTypeEnum, Object data) {
        return new InteractiveModel(interactiveSeq, interactiveTypeEnum.name(), new Data(data));
    }

    public static InteractiveModel of(InteractiveTypeEnum interactiveTypeEnum, Object data) {
        return new InteractiveModel(SequenceUtils.genInteractiveSeq(), interactiveTypeEnum.name(), new Data(data));
    }

    @ToString
    public static class Data implements Serializable{

        private String data;

        public Data(Object data) {
            setData(data);
        }

        public <T> T convert(Class<T> classOfT) {
            return new Gson().fromJson(data, classOfT);
        }

        public void setData(Object data) {
            this.data = new Gson().toJson(data);
        }
    }
}
