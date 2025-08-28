package com.zhao.easyJmeter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhao.easyJmeter.common.enumeration.TaskResultEnum;
import com.zhao.easyJmeter.dto.task.CreateOrUpdateTaskDTO;
import com.zhao.easyJmeter.dto.task.JmeterAggregateReportDTO;
import com.zhao.easyJmeter.dto.task.ModifyTaskDTO;
import com.zhao.easyJmeter.model.ReportDO;
import com.zhao.easyJmeter.model.TaskDO;
import com.zhao.easyJmeter.vo.CutFileVO;
import com.zhao.easyJmeter.vo.HistoryTaskVO;
import com.zhao.easyJmeter.vo.TaskInfoVO;

import java.util.List;
import java.util.Map;

public interface TaskService {

    boolean createTask(CreateOrUpdateTaskDTO taskDTO);

    boolean updateTaskResult(TaskDO taskDO, TaskResultEnum result);

    TaskDO getTaskById(Integer id);

    Map<String, List<CutFileVO>> cutCsv(TaskDO taskDO);

    TaskDO getTaskByTaskId(String taskId);

    boolean stopTask(String taskId);

    boolean modifyQPSLimit(ModifyTaskDTO validator);

    TaskInfoVO getTaskInfo(String taskId);

    List<Map<String, Object>> getTaskLogByTaskId(String taskId);

    ReportDO getTaskReportByTaskId(String taskId);

    IPage<HistoryTaskVO> getHistoryTask(Integer current,String jmeterCase, String taskId, String startTime, String endTime, Integer result);

    boolean deleteTasks(List<Integer> ids);

    List<TaskDO> getTasksByCaseId(Integer caseId);

    void aggregateReportAdd(JmeterAggregateReportDTO jmeterAggregateReportDTO);
}
