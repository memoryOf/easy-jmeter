package com.zhao.easyJmeter.controller.v1;

import com.zhao.easyJmeter.model.SceneDO;
import com.zhao.easyJmeter.service.SceneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scenes")
public class SceneController {

    @Autowired
    private SceneService sceneService;

    @GetMapping
    public Object getScenes() {
        List<SceneDO> scenes = sceneService.list();
        return new Object() {
            public int code = 0;
            public String msg = "success";
            public List<SceneDO> data = scenes;
        };
    }

    @PostMapping
    public Object createScene(@RequestBody SceneDO scene) {
        boolean result = sceneService.save(scene);
        return new Object() {
            public int code = result ? 0 : 1;
            public String msg = result ? "创建成功" : "创建失败";
        };
    }

    @PutMapping("/{id}")
    public Object updateScene(@PathVariable Long id, @RequestBody SceneDO scene) {
        scene.setId(id);
        boolean result = sceneService.updateById(scene);
        return new Object() {
            public int code = result ? 0 : 1;
            public String msg = result ? "更新成功" : "更新失败";
        };
    }

    @DeleteMapping("/{id}")
    public Object deleteScene(@PathVariable Long id) {
        boolean result = sceneService.removeById(id);
        return new Object() {
            public int code = result ? 0 : 1;
            public String msg = result ? "删除成功" : "删除失败";
        };
    }
}