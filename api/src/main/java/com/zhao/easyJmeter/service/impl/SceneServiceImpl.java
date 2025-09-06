package com.zhao.easyJmeter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhao.easyJmeter.mapper.SceneMapper;
import com.zhao.easyJmeter.model.SceneDO;
import com.zhao.easyJmeter.service.SceneService;
import org.springframework.stereotype.Service;

@Service
public class SceneServiceImpl extends ServiceImpl<SceneMapper, SceneDO> implements SceneService {
}