package com.zhao.easyJmeter.common.jmeter.component;

import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.visualizers.ViewResultsFullVisualizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 察看结果树组件实现
 */
public class ResultCollectorComponent extends JMeterComponentBase {
    
    public ResultCollectorComponent() {
        this.setComponentType("ResultCollector");
        this.setMetadata(createMetadata());
        this.setProperties(getDefaultProperties());
    }
    
    @Override
    public String getTestElementClass() {
        return "org.apache.jmeter.reporters.ResultCollector";
    }
    
    @Override
    public String getGuiClass() {
        return "org.apache.jmeter.visualizers.ViewResultsFullVisualizer";
    }
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        
        // 验证文件名（如果要保存到文件）
        Object filename = getProperties().get("filename");
        if (filename != null && !filename.toString().trim().isEmpty()) {
            String file = filename.toString().trim();
            if (!file.endsWith(".jtl") && !file.endsWith(".xml") && !file.endsWith(".csv")) {
                result.addWarning("filename", "建议使用 .jtl、.xml 或 .csv 扩展名");
            }
        }
        
        return result;
    }
    
    @Override
    public TestElement toTestElement() {
        ResultCollector resultCollector = new ResultCollector();
        resultCollector.setProperty(TestElement.TEST_CLASS, ResultCollector.class.getName());
        resultCollector.setProperty(TestElement.GUI_CLASS, ViewResultsFullVisualizer.class.getName());
        resultCollector.setName(this.getName());
        resultCollector.setEnabled(this.isEnabled());
        
        // 设置文件名
        Object filename = getProperties().get("filename");
        if (filename != null && !filename.toString().trim().isEmpty()) {
            resultCollector.setFilename(filename.toString());
        }
        
        // 设置是否记录错误
        Object errorsOnly = getProperties().get("errorsOnly");
        if (errorsOnly != null) {
            resultCollector.setErrorLogging(Boolean.parseBoolean(errorsOnly.toString()));
        }
        
        // 设置是否记录成功
        Object successOnly = getProperties().get("successOnly");
        if (successOnly != null) {
            resultCollector.setSuccessOnlyLogging(Boolean.parseBoolean(successOnly.toString()));
        }
        
        return resultCollector;
    }
    
    @Override
    public void fromTestElement(TestElement element) {
        if (!(element instanceof ResultCollector)) {
            throw new IllegalArgumentException("Element must be a ResultCollector");
        }
        
        ResultCollector resultCollector = (ResultCollector) element;
        this.setName(resultCollector.getName());
        this.setEnabled(resultCollector.isEnabled());
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("filename", resultCollector.getFilename());
        properties.put("errorsOnly", resultCollector.isErrorLogging());
        properties.put("successOnly", resultCollector.isSuccessOnlyLogging());
        
        this.setProperties(properties);
    }
    
    @Override
    public Map<String, Object> getDefaultProperties() {
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("filename", "");
        defaults.put("errorsOnly", false);
        defaults.put("successOnly", false);
        return defaults;
    }
    
    @Override
    public List<PropertyDefinition> getSupportedProperties() {
        List<PropertyDefinition> properties = new ArrayList<>();
        
        properties.add(PropertyDefinition.builder()
                .name("filename")
                .displayName("文件名")
                .description("保存测试结果的文件路径（可选）")
                .type(PropertyDefinition.PropertyType.STRING)
                .defaultValue("")
                .group("基本设置")
                .order(1)
                .placeholder("例如：results.jtl")
                .build());
                
        properties.add(PropertyDefinition.builder()
                .name("errorsOnly")
                .displayName("只记录错误")
                .description("是否只记录失败的结果")
                .type(PropertyDefinition.PropertyType.BOOLEAN)
                .defaultValue(false)
                .group("记录设置")
                .order(2)
                .build());
                
        properties.add(PropertyDefinition.builder()
                .name("successOnly")
                .displayName("只记录成功")
                .description("是否只记录成功的结果")
                .type(PropertyDefinition.PropertyType.BOOLEAN)
                .defaultValue(false)
                .group("记录设置")
                .order(3)
                .build());
        
        return properties;
    }
    
    private ComponentMetadata createMetadata() {
        return ComponentMetadata.builder()
                .displayName("察看结果树")
                .description("以树状结构显示测试结果")
                .category("监听器")
                .icon("icon-view-results-tree")
                .container(false)
                .acceptedChildren(new String[]{})
                .acceptedParents(new String[]{"ThreadGroup"})
                .sortOrder(50)
                .builtin(true)
                .build();
    }
}
