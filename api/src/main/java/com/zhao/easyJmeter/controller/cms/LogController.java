package com.zhao.easyJmeter.controller.cms;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhao.easyJmeter.common.util.PageUtil;
import com.zhao.easyJmeter.dto.log.QueryLogDTO;
import com.zhao.easyJmeter.dto.query.BasePageDTO;
import com.zhao.easyJmeter.model.LogDO;
import com.zhao.easyJmeter.service.LogService;
import com.zhao.easyJmeter.vo.PageResponseVO;
import io.github.talelin.core.annotation.GroupRequired;
import io.github.talelin.core.annotation.PermissionMeta;
import io.github.talelin.core.annotation.PermissionModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author pedro@TaleLin
 * @author Juzi@TaleLin
 */
@RestController
@RequestMapping("/cms/log")
@PermissionModule(value = "日志")
@Validated
public class LogController {
    @Autowired
    private LogService logService;

    @GetMapping("")
    @GroupRequired
    @PermissionMeta(value = "查询所有日志")
    public PageResponseVO<LogDO> getLogs(QueryLogDTO dto) {
        IPage<LogDO> iPage = logService.getLogPage(
                dto.getPage(), dto.getCount(),
                dto.getName(), dto.getStart(),
                dto.getEnd()
        );
        return PageUtil.build(iPage);
    }

    @GetMapping("/search")
    @GroupRequired
    @PermissionMeta(value = "搜索日志")
    public PageResponseVO<LogDO> searchLogs(QueryLogDTO dto) {
        IPage<LogDO> iPage = logService.searchLogPage(
                dto.getPage(), dto.getCount(),
                dto.getName(), dto.getKeyword(),
                dto.getStart(), dto.getEnd()
        );
        return PageUtil.build(iPage);
    }

    @GetMapping("/users")
    @GroupRequired
    @PermissionMeta(value = "查询日志记录的用户")
    public PageResponseVO<String> getUsers(@Validated BasePageDTO dto) {
        IPage<String> iPage = logService.getUserNamePage(dto.getPage(), dto.getCount());
        return PageUtil.build(iPage);
    }
}
