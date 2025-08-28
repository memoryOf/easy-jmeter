package com.zhao.easyJmeter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhao.easyJmeter.common.enumeration.JmeterStatusEnum;
import com.zhao.easyJmeter.model.TaskLogDO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskLogMapper extends BaseMapper<TaskLogDO> {

    List<TaskLogDO> search(String taskId, Integer jCase, JmeterStatusEnum status, String address, Boolean result);

}
