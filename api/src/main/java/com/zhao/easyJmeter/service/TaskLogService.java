package com.zhao.easyJmeter.service;

import com.zhao.easyJmeter.common.enumeration.JmeterStatusEnum;
import com.zhao.easyJmeter.model.TaskLogDO;

import java.util.List;

public interface TaskLogService {

    List<TaskLogDO> getTaskLog(String taskId, Integer jCase, JmeterStatusEnum status, String address, Boolean result);

    boolean createTaskLog(TaskLogDO taskLogDO);

    boolean updateTaskLog(TaskLogDO taskLogDO, Boolean result);
}
