package com.zhao.easyJmeter.dto.task;

import com.zhao.easyJmeter.model.TaskDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskMachineDTO {

    private TaskDO taskDO;

    private String machineIp;

    private Boolean result;

    private Integer status;

}
