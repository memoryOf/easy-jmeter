package com.zhao.easyJmeter.service;

import com.zhao.easyJmeter.common.jmeter.component.JMeterComponentBase;
import com.zhao.easyJmeter.common.jmeter.component.JMeterComponentFactory;
import com.zhao.easyJmeter.common.jmeter.component.ValidationResult;

import java.util.List;

/**
 * JMeter脚本管理服务接口
 */
public interface JMeterScriptService {
    
    /**
     * 从组件树生成JMX脚本内容
     */
    String generateJmxScript(JMeterComponentBase rootComponent);
    
    /**
     * 解析JMX脚本为组件树
     */
    JMeterComponentBase parseJmxScript(String jmxContent);
    
    /**
     * 验证组件树的完整性
     */
    ValidationResult validateComponentTree(JMeterComponentBase rootComponent);
    
    /**
     * 保存脚本到文件
     */
    String saveScript(Integer caseId, JMeterComponentBase rootComponent);
    
    /**
     * 从文件加载脚本
     */
    JMeterComponentBase loadScript(Integer caseId);
    
    /**
     * 获取脚本预览
     */
    String getScriptPreview(JMeterComponentBase rootComponent);
    
    /**
     * 从模板创建脚本
     */
    JMeterComponentBase createFromTemplate(String templateName);
    
    /**
     * 获取可用的脚本模板
     */
    List<ScriptTemplate> getAvailableTemplates();
    
    /**
     * 脚本模板定义
     */
    class ScriptTemplate {
        private String name;
        private String displayName;
        private String description;
        private String category;
        private JMeterComponentBase rootComponent;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public JMeterComponentBase getRootComponent() { return rootComponent; }
        public void setRootComponent(JMeterComponentBase rootComponent) { this.rootComponent = rootComponent; }
    }
}
