package com.zhao.easyJmeter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhao.easyJmeter.model.CaseDO;
import com.zhao.easyJmeter.vo.CaseInfoVO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaseMapper extends BaseMapper<CaseDO> {
    List<CaseInfoVO> select(Integer id);

    List<CaseDO> selectByProject(Integer project);
}
