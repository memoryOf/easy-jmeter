package com.zhao.easyJmeter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhao.easyJmeter.common.mybatis.Page;
import com.zhao.easyJmeter.model.TaskDO;
import com.zhao.easyJmeter.vo.HistoryTaskVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskMapper extends BaseMapper<TaskDO> {

    IPage<HistoryTaskVO> selectHistory(Page page, @Param("taskId") String taskId,
                                       @Param("startTime") String startTime, @Param("endTime") String endTime,
                                       @Param("result") Integer result, @Param("jmeterCase") String jmeterCase);

    Integer selectSumDuration(@Param("taskId") String taskId);

    TaskDO selectByIdIncludeDelete(@Param("id") Integer id);
}
