package com.future94.gothrough.server.adapter;

import com.future94.gothrough.common.utils.Optional;
import com.future94.gothrough.protocol.channel.GoThroughSocketChannel;
import com.future94.gothrough.protocol.model.InteractiveModel;
import com.future94.gothrough.server.handler.InteractiveProcessHandler;
import com.future94.gothrough.server.handler.PassValueHandler;
import com.future94.gothrough.server.process.ProcessStatusDTO;
import com.future94.gothrough.server.service.ServerService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

/**
 * @author weilai
 */
@Slf4j
public class ReadAheadPassValueAdapter implements ClientSocketAdapter {

    private ServerService<InteractiveModel, InteractiveModel> serverService;

    private List<PassValueHandler<InteractiveModel, InteractiveModel>> pipelineList = new LinkedList<>();

    public ReadAheadPassValueAdapter(ServerService<InteractiveModel, InteractiveModel> serverService) {
        this.serverService = serverService;
        this.pipelineList.add(InteractiveProcessHandler.getInstance());
    }

    @Override
    public void process(Socket acceptClientSocket) throws Exception {
        // 建立交互通道
        GoThroughSocketChannel<InteractiveModel, InteractiveModel> goThroughSocketChannel;
        try {
            goThroughSocketChannel = this.serverService.createGoThroughSocketChannel(acceptClientSocket);
        } catch (Exception e) {
            log.error("创建socket通道异常", e);
            throw e;
        }

        Optional<InteractiveModel> optional;
        try {
            InteractiveModel read = goThroughSocketChannel.read();
            optional = Optional.of(read);
        } catch (Exception e) {
            log.error("读取数据异常", e);
            throw e;
        }

        for (PassValueHandler<InteractiveModel, InteractiveModel> passValueHandler : pipelineList) {
            // 执行失败时，让下一个handler尝试执行
            ProcessStatusDTO processStatus = passValueHandler.process(goThroughSocketChannel, optional);
            if (processStatus.isClose()) {
                try {
                    goThroughSocketChannel.close();
                } catch (IOException e) {
                    log.error("关闭socket异常", e);
                    return;
                }
            }
            if (!processStatus.isNext()) {
                break;
            }
        }
    }
}
