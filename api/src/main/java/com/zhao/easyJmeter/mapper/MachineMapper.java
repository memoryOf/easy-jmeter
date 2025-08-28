package com.zhao.easyJmeter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhao.easyJmeter.common.mybatis.Page;
import com.zhao.easyJmeter.model.MachineDO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface MachineMapper  extends BaseMapper<MachineDO> {

    IPage<MachineDO> selectByName(Page page, @Param("name") String name);

    MachineDO selectByAddress(String address);

    MachineDO selectByClientId(String clientId);

    ArrayList<MachineDO> selectAll();

    ArrayList<MachineDO> executable();
}
