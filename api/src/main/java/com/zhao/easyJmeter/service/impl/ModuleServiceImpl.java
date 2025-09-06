package com.zhao.easyJmeter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhao.easyJmeter.mapper.ModuleMapper;
import com.zhao.easyJmeter.model.ModuleDO;
import com.zhao.easyJmeter.service.ModuleService;
import org.springframework.stereotype.Service;

@Service
public class ModuleServiceImpl extends ServiceImpl<ModuleMapper, ModuleDO> implements ModuleService {
}