package com.zhao.easyJmeter.common.jmeter.links;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhao.easyJmeter.common.enumeration.JmeterStatusEnum;
import com.zhao.easyJmeter.common.jmeter.JmeterExternal;
import com.zhao.easyJmeter.common.jmeter.LinkStrategy;
import com.zhao.easyJmeter.dto.task.TaskMachineDTO;
import com.zhao.easyJmeter.model.TaskDO;
import com.zhao.easyJmeter.service.JFileService;
import io.socket.client.Socket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CollectLink extends Thread implements LinkStrategy {

    private final JFileService jFileService;
    private final Socket socket;
    private TaskDO taskDO;

    public CollectLink(Socket socket, JFileService jFileService) {
        this.socket = socket;
        this.jFileService = jFileService;
    }

    @Override
    public void setTask(TaskDO taskDO) {
        this.taskDO = taskDO;
    }

    @Override
    public void run() {
        try {
            log.info("===" + this.taskDO.getTaskId() + "_" + JmeterStatusEnum.COLLECT.getDesc() + "===");
            new JmeterExternal(socket).collect(this.taskDO, jFileService);
            this.reportSuccess();
        } catch (Exception e) {
            log.error("收集环节发生" + e.getClass().getName() + "异常：" +e.getMessage() + "，任务ID：" + this.taskDO.getTaskId());
            log.error("error", e);
            this.reportFail();
        }
    }

    @Override
    public Boolean reportSuccess() throws JsonProcessingException {
        TaskMachineDTO taskMachineDTO = new TaskMachineDTO();
        taskMachineDTO.setTaskDO(taskDO);
        taskMachineDTO.setMachineIp(new JmeterExternal(socket).getAddress());
        taskMachineDTO.setResult(true);
        String message = new ObjectMapper().writeValueAsString(taskMachineDTO);
        socket.emit("collectFinish", message);
        return true;
    }

    @Override
    public Boolean reportFail() {
        // 发送失败消息
        TaskMachineDTO taskMachineDTO = new TaskMachineDTO();
        taskMachineDTO.setTaskDO(taskDO);
        taskMachineDTO.setMachineIp(new JmeterExternal(socket).getAddress());
        taskMachineDTO.setResult(false);
        taskMachineDTO.setStatus(JmeterStatusEnum.COLLECT.getValue());
        try {
            String message = new ObjectMapper().writeValueAsString(taskMachineDTO);
            socket.emit("linkFail", message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public Boolean interruptThread() {
        // 终止线程
        ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
        int noThreads = currentGroup.activeCount();
        Thread[] lstThreads = new Thread[noThreads];
        currentGroup.enumerate(lstThreads);
        for (int i = 0; i < noThreads; i++) {
            if (lstThreads[i].getName().equals(taskDO.getTaskId()+"_"+ JmeterStatusEnum.COLLECT.getDesc())) {
                lstThreads[i].interrupt();
            }
        }
        return true;
    }
}
