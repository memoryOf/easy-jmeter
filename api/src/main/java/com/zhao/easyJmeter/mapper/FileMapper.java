package com.zhao.easyJmeter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhao.easyJmeter.model.FileDO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author pedro@TaleLin
 */
@Repository
public interface FileMapper extends BaseMapper<FileDO> {

    FileDO selectByMd5(@Param("md5") String md5);

    int selectCountByMd5(@Param("md5") String md5);
}
