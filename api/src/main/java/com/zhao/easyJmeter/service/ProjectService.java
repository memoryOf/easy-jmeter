package com.zhao.easyJmeter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhao.easyJmeter.dto.project.CreateOrUpdateProjectDTO;
import com.zhao.easyJmeter.model.ProjectDO;

import java.util.List;


public interface ProjectService {

    IPage<ProjectDO> getProjectByName(Integer current, String name);

    boolean createProject(CreateOrUpdateProjectDTO validator);

    boolean updateProject(ProjectDO projectDO, CreateOrUpdateProjectDTO validator);

    ProjectDO getById(Integer id);

    boolean deleteProject(Integer id);

    List<ProjectDO> getAll();

    Integer getCaseCount(Integer id);
}
