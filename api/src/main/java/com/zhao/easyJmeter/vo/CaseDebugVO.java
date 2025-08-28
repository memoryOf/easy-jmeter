package com.zhao.easyJmeter.vo;

import com.zhao.easyJmeter.common.enumeration.DebugTypeEnum;
import com.zhao.easyJmeter.common.jmeter.xstream.TestResults;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CaseDebugVO {
    private Integer caseId;
    private Long debugId;
    private DebugTypeEnum type;
    private TestResults result;
    private String log;
}
