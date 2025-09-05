package com.zhao.easyJmeter.controller.v1;

import com.zhao.easyJmeter.common.jmeter.component.JMeterComponentBase;
import com.zhao.easyJmeter.common.jmeter.component.ValidationResult;
import com.zhao.easyJmeter.service.JMeterScriptService;
import com.zhao.easyJmeter.vo.CreatedVO;
import com.zhao.easyJmeter.vo.UpdatedVO;
import io.github.talelin.core.annotation.LoginRequired;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * JMeter脚本编辑器控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/jmeter/script-editor")
@Api(tags = "JMeter脚本编辑器")
@RequiredArgsConstructor
@Validated
public class JMeterScriptEditorController {
    
    private final JMeterScriptService scriptService;
    
    @PostMapping("/generate-jmx")
    @ApiOperation(value = "生成JMX脚本", notes = "根据组件树生成JMeter JMX脚本内容")
    @LoginRequired
    public Map<String, Object> generateJmxScript(@RequestBody JMeterComponentBase rootComponent) {
        try {
            String jmxContent = scriptService.generateJmxScript(rootComponent);
            return Map.of(
                "success", true,
                "jmxContent", jmxContent,
                "message", "JMX脚本生成成功"
            );
        } catch (Exception e) {
            log.error("Generate JMX script failed", e);
            return Map.of(
                "success", false,
                "message", "生成JMX脚本失败: " + e.getMessage()
            );
        }
    }
    
    @PostMapping("/parse-jmx")
    @ApiOperation(value = "解析JMX脚本", notes = "将JMX脚本内容解析为组件树")
    @LoginRequired
    public Map<String, Object> parseJmxScript(@RequestBody Map<String, String> request) {
        try {
            String jmxContent = request.get("jmxContent");
            if (jmxContent == null || jmxContent.trim().isEmpty()) {
                return Map.of(
                    "success", false,
                    "message", "JMX脚本内容不能为空"
                );
            }
            
            JMeterComponentBase rootComponent = scriptService.parseJmxScript(jmxContent);
            return Map.of(
                "success", true,
                "rootComponent", rootComponent,
                "message", "JMX脚本解析成功"
            );
        } catch (Exception e) {
            log.error("Parse JMX script failed", e);
            return Map.of(
                "success", false,
                "message", "解析JMX脚本失败: " + e.getMessage()
            );
        }
    }
    
    @PostMapping("/save/{caseId}")
    @ApiOperation(value = "保存脚本", notes = "将组件树保存为JMX脚本文件")
    @LoginRequired
    public Map<String, Object> saveScript(
            @PathVariable Integer caseId,
            @RequestBody JMeterComponentBase rootComponent) {
        try {
            String filePath = scriptService.saveScript(caseId, rootComponent);
            return Map.of(
                "success", true,
                "filePath", filePath,
                "message", "脚本保存成功"
            );
        } catch (Exception e) {
            log.error("Save script failed for case: {}", caseId, e);
            return Map.of(
                "success", false,
                "message", "保存脚本失败: " + e.getMessage()
            );
        }
    }
    
    @GetMapping("/load/{caseId}")
    @ApiOperation(value = "加载脚本", notes = "从文件加载组件树")
    @LoginRequired
    public Map<String, Object> loadScript(@PathVariable Integer caseId) {
        try {
            JMeterComponentBase rootComponent = scriptService.loadScript(caseId);
            if (rootComponent == null) {
                return Map.of(
                    "success", false,
                    "message", "未找到用例对应的脚本文件"
                );
            }
            
            return Map.of(
                "success", true,
                "rootComponent", rootComponent,
                "message", "脚本加载成功"
            );
        } catch (Exception e) {
            log.error("Load script failed for case: {}", caseId, e);
            return Map.of(
                "success", false,
                "message", "加载脚本失败: " + e.getMessage()
            );
        }
    }
    
    @PostMapping("/preview")
    @ApiOperation(value = "脚本预览", notes = "生成脚本的文本预览")
    @LoginRequired
    public Map<String, Object> getScriptPreview(@RequestBody JMeterComponentBase rootComponent) {
        try {
            String preview = scriptService.getScriptPreview(rootComponent);
            return Map.of(
                "success", true,
                "preview", preview,
                "message", "预览生成成功"
            );
        } catch (Exception e) {
            log.error("Generate script preview failed", e);
            return Map.of(
                "success", false,
                "message", "生成预览失败: " + e.getMessage()
            );
        }
    }
    
    @GetMapping("/templates")
    @ApiOperation(value = "获取脚本模板", notes = "获取可用的脚本模板列表")
    @LoginRequired
    public List<JMeterScriptService.ScriptTemplate> getScriptTemplates() {
        return scriptService.getAvailableTemplates();
    }
    
    @PostMapping("/from-template/{templateName}")
    @ApiOperation(value = "从模板创建", notes = "从指定模板创建组件树")
    @LoginRequired
    public Map<String, Object> createFromTemplate(@PathVariable String templateName) {
        try {
            JMeterComponentBase rootComponent = scriptService.createFromTemplate(templateName);
            return Map.of(
                "success", true,
                "rootComponent", rootComponent,
                "message", "从模板创建成功"
            );
        } catch (Exception e) {
            log.error("Create from template failed: {}", templateName, e);
            return Map.of(
                "success", false,
                "message", "从模板创建失败: " + e.getMessage()
            );
        }
    }
    
    @PostMapping("/validate-tree")
    @ApiOperation(value = "验证脚本树", notes = "验证整个脚本组件树的完整性")
    @LoginRequired
    public ValidationResult validateScriptTree(@RequestBody JMeterComponentBase rootComponent) {
        return scriptService.validateComponentTree(rootComponent);
    }
    
    @PostMapping("/export")
    @ApiOperation(value = "导出脚本", notes = "导出组件树为可下载的JMX文件")
    @LoginRequired
    public Map<String, Object> exportScript(@RequestBody ExportScriptRequest request) {
        try {
            String jmxContent = scriptService.generateJmxScript(request.getRootComponent());
            
            return Map.of(
                "success", true,
                "filename", request.getFilename() != null ? request.getFilename() : "test-plan.jmx",
                "content", jmxContent,
                "contentType", "application/xml",
                "message", "脚本导出成功"
            );
        } catch (Exception e) {
            log.error("Export script failed", e);
            return Map.of(
                "success", false,
                "message", "导出脚本失败: " + e.getMessage()
            );
        }
    }
    
    /**
     * 导出脚本请求DTO
     */
    public static class ExportScriptRequest {
        private JMeterComponentBase rootComponent;
        private String filename;
        
        public JMeterComponentBase getRootComponent() { return rootComponent; }
        public void setRootComponent(JMeterComponentBase rootComponent) { this.rootComponent = rootComponent; }
        
        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }
    }
}
