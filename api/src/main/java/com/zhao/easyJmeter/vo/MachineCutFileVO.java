package com.zhao.easyJmeter.vo;

import com.zhao.easyJmeter.model.TaskDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MachineCutFileVO {
    private Map<String, List<CutFileVO>> machineDOCutFileVOListMap;

    private TaskDO taskDO;

    private Boolean needCut;
}
