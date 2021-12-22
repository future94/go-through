package com.future94.gothrough.protocol.model.dto;

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
public class ClientControlDTO {

    private Integer serverExposedListenPort;
}
