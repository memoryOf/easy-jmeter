package com.zhao.easyJmeter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhao.easyJmeter.common.LocalUser;
import com.zhao.easyJmeter.common.mybatis.Page;
import com.zhao.easyJmeter.dto.project.CreateOrUpdateProjectDTO;
import com.zhao.easyJmeter.mapper.CaseMapper;
import com.zhao.easyJmeter.mapper.ProjectMapper;
import com.zhao.easyJmeter.model.CaseDO;
import com.zhao.easyJmeter.model.ProjectDO;
import com.zhao.easyJmeter.model.UserDO;
import com.zhao.easyJmeter.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private CaseMapper caseMapper;

    @Override
    public IPage<ProjectDO> getProjectByName(Integer current, String name) {
        Page page = new Page(current, 10);
        IPage<ProjectDO> projects = projectMapper.selectByName(page, name);
        return projects;
    }

    @Override
    public boolean createProject(CreateOrUpdateProjectDTO validator) {
        ProjectDO projectDO = new ProjectDO();
        projectDO.setName(validator.getName());
        UserDO user = LocalUser.getLocalUser();
        projectDO.setCreator(user.getId());
        projectDO.setDescription(validator.getDescription());
        return projectMapper.insert(projectDO) > 0;
    }

    @Override
    public boolean updateProject(ProjectDO project, CreateOrUpdateProjectDTO validator) {
        project.setName(validator.getName());
        project.setDescription(validator.getDescription());
        return projectMapper.updateById(project) > 0;
    }

    @Override
    public ProjectDO getById(Integer id) {
        return projectMapper.selectById(id);
    }

    @Override
    @Transactional
    public boolean deleteProject(Integer id) {
        List<CaseDO> caseList = caseMapper.selectByProject(id);
        caseList.forEach(caseDO -> {
            caseMapper.deleteById(caseDO.getId());
        });
        return projectMapper.deleteById(id) > 0;
    }

    @Override
    public List<ProjectDO> getAll() {
        return projectMapper.getAll();
    }

    @Override
    public Integer getCaseCount(Integer id) {
        QueryWrapper<CaseDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("project", id);
        return caseMapper.selectCount(queryWrapper);
    }
}
