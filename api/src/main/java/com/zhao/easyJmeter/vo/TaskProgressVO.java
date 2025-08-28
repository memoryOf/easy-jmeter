package com.zhao.easyJmeter.vo;

import com.zhao.easyJmeter.common.enumeration.JmeterStatusEnum;
import com.zhao.easyJmeter.common.enumeration.TaskResultEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskProgressVO {

    private String taskId;

    private JmeterStatusEnum status;

    private HashMap<String, Integer> taskProgress;

    private TaskResultEnum taskResult;
}
