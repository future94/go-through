package com.future94.gothrough.server.process;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author weilai
 */
@Data
@AllArgsConstructor
public class ProcessStatusDTO {

    private boolean close;

    private boolean next;

}
