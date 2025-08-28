package com.zhao.easyJmeter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhao.easyJmeter.model.UserGroupDO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author pedro@TaleLin
 */
@Repository
public interface UserGroupMapper extends BaseMapper<UserGroupDO> {

    int insertBatch(@Param("relations") List<UserGroupDO> relations);

    int deleteByUserId(@Param("user_id") Integer userId);
}
