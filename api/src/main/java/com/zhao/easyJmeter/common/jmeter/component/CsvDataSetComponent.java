package com.zhao.easyJmeter.common.jmeter.component;

import org.apache.jmeter.config.CSVDataSet;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CSV数据集配置组件实现
 */
public class CsvDataSetComponent extends JMeterComponentBase {
    
    public CsvDataSetComponent() {
        this.setComponentType("CSVDataSet");
        this.setMetadata(createMetadata());
        this.setProperties(getDefaultProperties());
    }
    
    @Override
    public String getTestElementClass() {
        return "org.apache.jmeter.config.CSVDataSet";
    }
    
    @Override
    public String getGuiClass() {
        return "org.apache.jmeter.testbeans.gui.TestBeanGUI";
    }
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        
        // 验证文件名
        Object filename = getProperties().get("filename");
        if (filename == null || !StringUtils.hasText(filename.toString())) {
            result.addError("filename", "文件名不能为空");
        }
        
        // 验证变量名
        Object variableNames = getProperties().get("variableNames");
        if (variableNames == null || !StringUtils.hasText(variableNames.toString())) {
            result.addError("variableNames", "变量名不能为空");
        }
        
        // 验证分隔符
        Object delimiter = getProperties().get("delimiter");
        if (delimiter == null || delimiter.toString().isEmpty()) {
            result.addWarning("delimiter", "建议设置分隔符");
        }
        
        return result;
    }
    
    @Override
    public TestElement toTestElement() {
        CSVDataSet csvDataSet = new CSVDataSet();
        csvDataSet.setProperty(TestElement.TEST_CLASS, CSVDataSet.class.getName());
        csvDataSet.setProperty(TestElement.GUI_CLASS, TestBeanGUI.class.getName());
        csvDataSet.setName(this.getName());
        csvDataSet.setEnabled(this.isEnabled());
        
        // 设置文件名
        Object filename = getProperties().get("filename");
        if (filename != null) {
            csvDataSet.setFilename(filename.toString());
        }
        
        // 设置变量名
        Object variableNames = getProperties().get("variableNames");
        if (variableNames != null) {
            csvDataSet.setVariableNames(variableNames.toString());
        }
        
        // 设置分隔符
        Object delimiter = getProperties().get("delimiter");
        if (delimiter != null) {
            csvDataSet.setDelimiter(delimiter.toString());
        }
        
        // 设置是否引用
        Object quotedData = getProperties().get("quotedData");
        if (quotedData != null) {
            csvDataSet.setQuotedData(Boolean.parseBoolean(quotedData.toString()));
        }
        
        // 设置是否循环
        Object recycle = getProperties().get("recycle");
        if (recycle != null) {
            csvDataSet.setRecycle(Boolean.parseBoolean(recycle.toString()));
        }
        
        // 设置遇到EOF时是否停止线程
        Object stopThread = getProperties().get("stopThread");
        if (stopThread != null) {
            csvDataSet.setStopThread(Boolean.parseBoolean(stopThread.toString()));
        }
        
        // 设置共享模式
        Object shareMode = getProperties().get("shareMode");
        if (shareMode != null) {
            csvDataSet.setShareMode(shareMode.toString());
        }
        
        // 设置是否忽略第一行
        Object ignoreFirstLine = getProperties().get("ignoreFirstLine");
        if (ignoreFirstLine != null) {
            csvDataSet.setIgnoreFirstLine(Boolean.parseBoolean(ignoreFirstLine.toString()));
        }
        
        return csvDataSet;
    }
    
    @Override
    public void fromTestElement(TestElement element) {
        if (!(element instanceof CSVDataSet)) {
            throw new IllegalArgumentException("Element must be a CSVDataSet");
        }
        
        CSVDataSet csvDataSet = (CSVDataSet) element;
        this.setName(csvDataSet.getName());
        this.setEnabled(csvDataSet.isEnabled());
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("filename", csvDataSet.getFilename());
        properties.put("variableNames", csvDataSet.getVariableNames());
        properties.put("delimiter", csvDataSet.getDelimiter());
        properties.put("quotedData", csvDataSet.getQuotedData());
        properties.put("recycle", csvDataSet.getRecycle());
        properties.put("stopThread", csvDataSet.getStopThread());
        properties.put("shareMode", csvDataSet.getShareMode());
        properties.put("ignoreFirstLine", csvDataSet.getProperty("ignoreFirstLine").getBooleanValue());
        
        this.setProperties(properties);
    }
    
    @Override
    public Map<String, Object> getDefaultProperties() {
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("filename", "");
        defaults.put("variableNames", "");
        defaults.put("delimiter", ",");
        defaults.put("quotedData", false);
        defaults.put("recycle", true);
        defaults.put("stopThread", false);
        defaults.put("shareMode", "shareMode.all");
        defaults.put("ignoreFirstLine", false);
        return defaults;
    }
    
    @Override
    public List<PropertyDefinition> getSupportedProperties() {
        List<PropertyDefinition> properties = new ArrayList<>();
        
        properties.add(PropertyDefinition.builder()
                .name("filename")
                .displayName("文件名")
                .description("CSV文件的路径")
                .type(PropertyDefinition.PropertyType.FILE)
                .defaultValue("")
                .required(true)
                .group("基本设置")
                .order(1)
                .build());
                
        properties.add(PropertyDefinition.builder()
                .name("variableNames")
                .displayName("变量名")
                .description("变量名列表，用逗号分隔")
                .type(PropertyDefinition.PropertyType.STRING)
                .defaultValue("")
                .required(true)
                .group("基本设置")
                .order(2)
                .placeholder("例如：username,password,email")
                .build());
                
        properties.add(PropertyDefinition.builder()
                .name("delimiter")
                .displayName("分隔符")
                .description("CSV文件的字段分隔符")
                .type(PropertyDefinition.PropertyType.STRING)
                .defaultValue(",")
                .group("基本设置")
                .order(3)
                .build());
                
        properties.add(PropertyDefinition.builder()
                .name("quotedData")
                .displayName("忽略引号")
                .description("是否忽略字段周围的引号")
                .type(PropertyDefinition.PropertyType.BOOLEAN)
                .defaultValue(false)
                .group("高级设置")
                .order(4)
                .build());
                
        properties.add(PropertyDefinition.builder()
                .name("recycle")
                .displayName("循环读取")
                .description("文件结束时是否从头开始读取")
                .type(PropertyDefinition.PropertyType.BOOLEAN)
                .defaultValue(true)
                .group("高级设置")
                .order(5)
                .build());
                
        properties.add(PropertyDefinition.builder()
                .name("stopThread")
                .displayName("EOF时停止线程")
                .description("遇到文件结束时是否停止线程")
                .type(PropertyDefinition.PropertyType.BOOLEAN)
                .defaultValue(false)
                .group("高级设置")
                .order(6)
                .build());
                
        properties.add(PropertyDefinition.builder()
                .name("shareMode")
                .displayName("共享模式")
                .description("数据共享模式")
                .type(PropertyDefinition.PropertyType.SELECT)
                .defaultValue("shareMode.all")
                .options(List.of(
                    PropertyDefinition.SelectOption.builder()
                        .label("所有线程").value("shareMode.all").description("所有线程共享").build(),
                    PropertyDefinition.SelectOption.builder()
                        .label("当前线程组").value("shareMode.group").description("当前线程组内共享").build(),
                    PropertyDefinition.SelectOption.builder()
                        .label("当前线程").value("shareMode.thread").description("仅当前线程使用").build()
                ))
                .group("高级设置")
                .order(7)
                .build());
                
        properties.add(PropertyDefinition.builder()
                .name("ignoreFirstLine")
                .displayName("忽略首行")
                .description("是否忽略CSV文件的第一行（通常是标题行）")
                .type(PropertyDefinition.PropertyType.BOOLEAN)
                .defaultValue(false)
                .group("高级设置")
                .order(8)
                .build());
        
        return properties;
    }
    
    private ComponentMetadata createMetadata() {
        return ComponentMetadata.builder()
                .displayName("CSV数据集配置")
                .description("从CSV文件读取测试数据")
                .category("配置元素")
                .icon("icon-csv-dataset")
                .container(false)
                .acceptedChildren(new String[]{})
                .acceptedParents(new String[]{"ThreadGroup"})
                .sortOrder(30)
                .builtin(true)
                .build();
    }
}
