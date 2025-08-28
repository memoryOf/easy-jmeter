package com.zhao.easyJmeter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhao.easyJmeter.common.enumeration.JmeterStatusEnum;
import com.zhao.easyJmeter.common.enumeration.LogLevelEnum;
import com.zhao.easyJmeter.common.enumeration.TaskResultEnum;
import com.zhao.easyJmeter.mapper.CaseMapper;
import com.zhao.easyJmeter.mapper.MachineMapper;
import com.zhao.easyJmeter.mapper.ProjectMapper;
import com.zhao.easyJmeter.mapper.TaskMapper;
import com.zhao.easyJmeter.model.MachineDO;
import com.zhao.easyJmeter.model.StatisticsDO;
import com.zhao.easyJmeter.model.TaskDO;
import com.zhao.easyJmeter.repository.ReportRepository;
import com.zhao.easyJmeter.repository.StatisticsRepository;
import com.zhao.easyJmeter.service.CommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommonServiceImpl implements CommonService {

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private CaseMapper caseMapper;

    @Autowired
    private MachineMapper machineMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private StatisticsRepository statisticsRepository;

    @Override
    public Map<String, Object> getEnum() {
        Map<String, Object> map = new HashMap<>();
        map.put("JmeterStatus", JmeterStatusEnum.toMapList());
        map.put("TaskResult", TaskResultEnum.toMapList());
        map.put("LogLevel", LogLevelEnum.toMapList());

        return map;
    }

    @Override
    public Map<String, Object> getTotal() {
        Map<String, Object> map = new HashMap<>();
        map.put("projectNum", projectMapper.selectCount(new QueryWrapper<>()));
        map.put("caseNum", caseMapper.selectCount(new QueryWrapper<>()));
        map.put("machineNum", machineMapper.selectCount(new QueryWrapper<MachineDO>().eq("is_online", true)));
        map.put("taskNum", taskMapper.selectCount(new QueryWrapper<TaskDO>().eq("result", 1)));
        map.put("durationSum", taskMapper.selectSumDuration(null));
        List<Long> totalSamples = statisticsRepository.getTotalSamplesSumForNonManual();
        if (totalSamples.isEmpty()) {
            map.put("totalSamples", 0);
        } else {
            map.put("totalSamples", totalSamples.get(0));
        }
        return map;
    }

    @Override
    public StatisticsDO getStatisticsById(String id) {
        return statisticsRepository.findById(id).orElse(null);
    }
}
