package com.zhao.easyJmeter.controller.v1;

import com.zhao.easyJmeter.model.ModuleDO;
import com.zhao.easyJmeter.service.ModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modules")
public class ModuleController {

    @Autowired
    private ModuleService moduleService;

    @GetMapping
    public Object getModules() {
        List<ModuleDO> modules = moduleService.list();
        return new Object() {
            public int code = 0;
            public String msg = "success";
            public List<ModuleDO> data = modules;
        };
    }

    @PostMapping
    public Object createModule(@RequestBody ModuleDO module) {
        boolean result = moduleService.save(module);
        return new Object() {
            public int code = result ? 0 : 1;
            public String msg = result ? "创建成功" : "创建失败";
        };
    }

    @PutMapping("/{id}")
    public Object updateModule(@PathVariable Long id, @RequestBody ModuleDO module) {
        module.setId(id);
        boolean result = moduleService.updateById(module);
        return new Object() {
            public int code = result ? 0 : 1;
            public String msg = result ? "更新成功" : "更新失败";
        };
    }

    @DeleteMapping("/{id}")
    public Object deleteModule(@PathVariable Long id) {
        boolean result = moduleService.removeById(id);
        return new Object() {
            public int code = result ? 0 : 1;
            public String msg = result ? "删除成功" : "删除失败";
        };
    }
}