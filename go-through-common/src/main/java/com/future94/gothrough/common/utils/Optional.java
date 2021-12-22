package com.future94.gothrough.common.utils;

/**
 * 操作对象，主要是让值能够通过引用进行传递
 */
public class Optional<T> {

    public static <T> Optional<T> of(T value) {
        return new Optional<T>(value);
    }

    public Optional(T value) {
        this.value = value;
    }

    private T value;

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

}
