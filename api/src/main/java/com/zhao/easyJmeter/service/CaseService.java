package com.zhao.easyJmeter.service;

import com.zhao.easyJmeter.common.enumeration.JmeterStatusEnum;
import com.zhao.easyJmeter.dto.jcase.CaseDebugDTO;
import com.zhao.easyJmeter.dto.jcase.CreateOrUpdateCaseDTO;
import com.zhao.easyJmeter.model.CaseDO;
import com.zhao.easyJmeter.vo.CaseInfoPlusVO;
import com.zhao.easyJmeter.vo.CaseInfoVO;

import java.io.IOException;
import java.util.List;

public interface CaseService {

    boolean createCase(CreateOrUpdateCaseDTO caseDTO);

    boolean updateCase(CaseDO caseDO, CreateOrUpdateCaseDTO caseDTO);

    CaseDO getById(Integer id);

    boolean deleteCase(Integer id);

    List<CaseInfoVO> selectCase(Integer id);

    CaseInfoPlusVO getCaseInfoById(Integer id);

    boolean updateCaseStatus(CaseDO caseDO, JmeterStatusEnum status);

    void debugCase(CaseDebugDTO caseDebugDTO) throws IOException;
}
