package com.future94.gothrough.server.handler;

import com.future94.gothrough.common.enums.InteractiveTypeEnum;
import com.future94.gothrough.common.utils.Optional;
import com.future94.gothrough.protocol.channel.GoThroughSocketChannel;
import com.future94.gothrough.protocol.model.InteractiveModel;
import com.future94.gothrough.protocol.model.dto.InteractiveResultDTO;
import com.future94.gothrough.server.process.ClientConnectProcess;
import com.future94.gothrough.server.process.ClientControlProcess;
import com.future94.gothrough.server.process.Process;
import com.future94.gothrough.server.process.ProcessStatusDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author weilai
 */
@Slf4j
public class InteractiveProcessHandler implements PassValueHandler<InteractiveModel, InteractiveModel> {

    private static final InteractiveProcessHandler INSTANCE = new InteractiveProcessHandler();

    private List<Process> pipelineList = new LinkedList<>();

    private InteractiveProcessHandler() {
        pipelineList.add(ClientControlProcess.getInstance());
        pipelineList.add(ClientConnectProcess.getInstance());
    }

    public static InteractiveProcessHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public ProcessStatusDTO process(GoThroughSocketChannel<InteractiveModel, InteractiveModel> goThroughSocketChannel, Optional<InteractiveModel> optional) throws Exception {
        InteractiveModel value = optional.getValue();
        log.info("接收到新消息：[ {} ]", value);

        if (Objects.isNull(value)) {
            log.warn("接收到新消息为null");
            return new ProcessStatusDTO(true, false);
        }

        for (Process process : this.pipelineList) {
            boolean wouldProc = process.isProcess(value);
            if (wouldProc) {
                boolean runSuccess = process.run(goThroughSocketChannel, value);
                if (runSuccess) {
                    return new ProcessStatusDTO(false, false);
                } else {
                    return new ProcessStatusDTO(true, false);
                }
            }
        }

        try {
            goThroughSocketChannel.writeAndFlush(InteractiveModel.of(value.getInteractiveSeq(), InteractiveTypeEnum.COMMON_REPLY, InteractiveResultDTO.buildInteractiveTypeNotFound()));
        } catch (Exception e) {
            log.error("发送消息时异常", e);
        }

        return new ProcessStatusDTO(true, false);
    }
}
