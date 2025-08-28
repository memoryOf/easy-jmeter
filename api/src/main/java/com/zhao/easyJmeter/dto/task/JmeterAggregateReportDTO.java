package com.zhao.easyJmeter.dto.task;

import com.zhao.easyJmeter.model.AggregateReportDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JmeterAggregateReportDTO {
    @NotNull(message = "{project.id.not-empty}")
    private Integer projectId;

    @NotEmpty(message = "{task.aggregate-reports.not-empty}")
    private List<AggregateReportDO> aggregateReports;

    private String text;
}