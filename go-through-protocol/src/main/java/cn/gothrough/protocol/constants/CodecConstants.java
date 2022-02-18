package cn.gothrough.protocol.constants;

/**
 * @author weilai
 */
public interface CodecConstants {

    int MAX_FRAME_LENGTH = 2 * 1024 * 1024;

    int LENGTH_FIELD_OFFSET = 0;

    int LENGTH_FIELD_LENGTH = 4;

    int INITIAL_BYTES_TO_STRIP = 0;

    int LENGTH_ADJUSTMENT = 0;
}
