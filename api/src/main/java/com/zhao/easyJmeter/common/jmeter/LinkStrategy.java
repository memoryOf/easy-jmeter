package com.zhao.easyJmeter.common.jmeter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zhao.easyJmeter.model.TaskDO;

public interface LinkStrategy {

    void setTask(TaskDO taskDO);

    Boolean reportSuccess() throws JsonProcessingException;

    Boolean reportFail();

    Boolean interruptThread();

}
