package com.zhao.easyJmeter.service.impl;

import com.zhao.easyJmeter.common.jmeter.component.*;
import com.zhao.easyJmeter.service.JMeterScriptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * JMeter脚本服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JMeterScriptServiceImpl implements JMeterScriptService {
    
    private final JMeterComponentFactory componentFactory;
    private static final String SCRIPT_BASE_PATH = "scripts/";
    
    @Override
    public String generateJmxScript(JMeterComponentBase rootComponent) {
        try {
            // 初始化JMeter环境
            initJMeterEnvironment();
            
            // 创建TestPlan
            TestPlan testPlan = new TestPlan("Test Plan");
            testPlan.setEnabled(true);
            
            // 构建HashTree
            HashTree testPlanTree = new HashTree();
            testPlanTree.add(testPlan);
            
            // 递归构建组件树
            if (rootComponent != null) {
                buildHashTree(testPlanTree, testPlan, rootComponent);
            }
            
            // 生成JMX内容（JMeter 5.5 使用 OutputStream）
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            SaveService.saveTree(testPlanTree, outputStream);
            return outputStream.toString("UTF-8");
            
        } catch (Exception e) {
            log.error("Failed to generate JMX script", e);
            throw new RuntimeException("生成JMX脚本失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public JMeterComponentBase parseJmxScript(String jmxContent) {
        try {
            // 初始化JMeter环境
            initJMeterEnvironment();
            
            // 解析JMX内容（将内容写入临时文件后使用 File 版本的 API）
            Path tempFile = Files.createTempFile("easyjmeter-", ".jmx");
            Files.write(tempFile, jmxContent.getBytes("UTF-8"));
            HashTree tree = SaveService.loadTree(tempFile.toFile());
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException ignore) {
            }
            
            // 转换为组件树
            return convertHashTreeToComponents(tree);
            
        } catch (Exception e) {
            log.error("Failed to parse JMX script", e);
            throw new RuntimeException("解析JMX脚本失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public ValidationResult validateComponentTree(JMeterComponentBase rootComponent) {
        ValidationResult result = new ValidationResult();
        
        if (rootComponent == null) {
            result.addError("root", "根组件不能为空");
            return result;
        }
        
        // 递归验证组件树
        validateComponentRecursively(rootComponent, result);
        
        return result;
    }
    
    @Override
    public String saveScript(Integer caseId, JMeterComponentBase rootComponent) {
        try {
            // 生成JMX脚本
            String jmxContent = generateJmxScript(rootComponent);
            
            // 确保目录存在
            Path scriptDir = Paths.get(SCRIPT_BASE_PATH);
            if (!Files.exists(scriptDir)) {
                Files.createDirectories(scriptDir);
            }
            
            // 保存到文件
            String filename = "case_" + caseId + "_editor.jmx";
            Path filePath = scriptDir.resolve(filename);
            Files.write(filePath, jmxContent.getBytes("UTF-8"));
            
            log.info("Script saved to: {}", filePath.toAbsolutePath());
            return filePath.toAbsolutePath().toString();
            
        } catch (Exception e) {
            log.error("Failed to save script for case: {}", caseId, e);
            throw new RuntimeException("保存脚本失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public JMeterComponentBase loadScript(Integer caseId) {
        try {
            String filename = "case_" + caseId + "_editor.jmx";
            Path filePath = Paths.get(SCRIPT_BASE_PATH).resolve(filename);
            
            if (!Files.exists(filePath)) {
                log.warn("Script file not found for case: {}", caseId);
                return null;
            }
            
            String jmxContent = new String(Files.readAllBytes(filePath), "UTF-8");
            return parseJmxScript(jmxContent);
            
        } catch (Exception e) {
            log.error("Failed to load script for case: {}", caseId, e);
            throw new RuntimeException("加载脚本失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String getScriptPreview(JMeterComponentBase rootComponent) {
        // 生成简化的预览内容
        StringBuilder preview = new StringBuilder();
        generatePreviewRecursively(rootComponent, preview, 0);
        return preview.toString();
    }
    
    @Override
    public JMeterComponentBase createFromTemplate(String templateName) {
        ScriptTemplate template = getTemplateByName(templateName);
        if (template == null) {
            throw new IllegalArgumentException("Template not found: " + templateName);
        }
        
        // 深度克隆模板组件
        return cloneComponent(template.getRootComponent());
    }
    
    @Override
    public List<ScriptTemplate> getAvailableTemplates() {
        List<ScriptTemplate> templates = new ArrayList<>();
        
        // HTTP基础模板
        templates.add(createHttpBasicTemplate());
        
        // 数据驱动模板
        templates.add(createDataDrivenTemplate());
        
        // API测试模板
        templates.add(createApiTestTemplate());
        
        return templates;
    }
    
    /**
     * 初始化JMeter环境
     */
    private void initJMeterEnvironment() {
        try {
            // 设置JMeter属性
            String jmeterHome = System.getProperty("user.dir") + "/apache-jmeter";
            JMeterUtils.setJMeterHome(jmeterHome);
            JMeterUtils.loadJMeterProperties(jmeterHome + "/bin/jmeter.properties");
            
        } catch (Exception e) {
            log.warn("Failed to initialize JMeter environment, using defaults", e);
        }
    }
    
    /**
     * 递归构建HashTree
     */
    private void buildHashTree(HashTree tree, TestElement parent, JMeterComponentBase component) {
        // 将组件转换为TestElement
        TestElement testElement = component.toTestElement();
        
        // 添加到树中
        HashTree subTree = tree.add(parent, testElement);
        
        // 递归处理子组件
        for (JMeterComponentBase child : component.getChildren()) {
            buildHashTree(subTree, testElement, child);
        }
    }
    
    /**
     * 将HashTree转换为组件树
     */
    private JMeterComponentBase convertHashTreeToComponents(HashTree tree) {
        if (tree.isEmpty()) {
            return null;
        }
        
        Object[] keys = tree.getArray();
        if (keys.length == 0) {
            return null;
        }
        
        for (Object key : keys) {
            if (key instanceof TestElement) {
                TestElement element = (TestElement) key;
                HashTree subTree = tree.getTree(key);
                
                JMeterComponentBase component = createComponentFromTestElement(element);
                if (component != null) {
                    convertChildComponents(subTree, component);
                    return component;
                }
            }
        }
        
        return null;
    }
    
    /**
     * 从TestElement创建组件
     */
    private JMeterComponentBase createComponentFromTestElement(TestElement element) {
        String className = element.getClass().getSimpleName();
        String componentType = mapClassNameToComponentType(className);
        if (componentType != null) {
            JMeterComponentBase component = componentFactory.createComponent(componentType);
            component.fromTestElement(element);
            component.setId(UUID.randomUUID().toString());
            return component;
        }
        return null;
    }
    
    /**
     * 递归转换子组件
     */
    private void convertChildComponents(HashTree subTree, JMeterComponentBase parent) {
        if (subTree.isEmpty()) {
            return;
        }
        
        Object[] keys = subTree.getArray();
        for (Object key : keys) {
            if (key instanceof TestElement) {
                TestElement element = (TestElement) key;
                JMeterComponentBase childComponent = createComponentFromTestElement(element);
                
                if (childComponent != null) {
                    childComponent.setParentId(parent.getId());
                    childComponent.setOrder(parent.getChildren().size());
                    parent.getChildren().add(childComponent);
                    
                    HashTree childTree = subTree.getTree(key);
                    convertChildComponents(childTree, childComponent);
                }
            }
        }
    }
    
    private void validateComponentRecursively(JMeterComponentBase component, ValidationResult result) {
        ValidationResult componentResult = componentFactory.validateComponent(component);
        if (!componentResult.isValid()) {
            result.getErrors().addAll(componentResult.getErrors());
            result.setValid(false);
        }
        result.getWarnings().addAll(componentResult.getWarnings());
        
        for (JMeterComponentBase child : component.getChildren()) {
            validateComponentRecursively(child, result);
        }
    }
    
    private void generatePreviewRecursively(JMeterComponentBase component, StringBuilder preview, int depth) {
        if (component == null) return;
        
        for (int i = 0; i < depth; i++) {
            preview.append("  ");
        }
        
        preview.append("- ").append(component.getMetadata().getDisplayName())
                .append(" [").append(component.getName()).append("]")
                .append("\n");
        
        for (JMeterComponentBase child : component.getChildren()) {
            generatePreviewRecursively(child, preview, depth + 1);
        }
    }
    
    private String mapClassNameToComponentType(String className) {
        switch (className) {
            case "ThreadGroup": return "ThreadGroup";
            case "HTTPSamplerProxy": return "HTTPSamplerProxy";
            case "HeaderManager": return "HeaderManager";
            case "CSVDataSet": return "CSVDataSet";
            case "ResponseAssertion": return "ResponseAssertion";
            case "ResultCollector": return "ResultCollector";
            default: return null;
        }
    }
    
    private ScriptTemplate getTemplateByName(String templateName) {
        return getAvailableTemplates().stream()
                .filter(template -> template.getName().equals(templateName))
                .findFirst()
                .orElse(null);
    }
    
    private JMeterComponentBase cloneComponent(JMeterComponentBase original) {
        if (original == null) return null;
        
        JMeterComponentBase cloned = componentFactory.createComponent(
                original.getComponentType(),
                original.getName(),
                new HashMap<>(original.getProperties())
        );
        
        for (JMeterComponentBase child : original.getChildren()) {
            JMeterComponentBase clonedChild = cloneComponent(child);
            clonedChild.setParentId(cloned.getId());
            cloned.getChildren().add(clonedChild);
        }
        
        return cloned;
    }
    
    // Template creation methods
    private ScriptTemplate createHttpBasicTemplate() {
        ThreadGroupComponent threadGroup = new ThreadGroupComponent();
        threadGroup.setId(UUID.randomUUID().toString());
        threadGroup.setName("线程组");
        
        HttpRequestComponent httpRequest = new HttpRequestComponent();
        httpRequest.setId(UUID.randomUUID().toString());
        httpRequest.setName("HTTP请求");
        httpRequest.setParentId(threadGroup.getId());
        
        Map<String, Object> httpProps = httpRequest.getDefaultProperties();
        httpProps.put("domain", "httpbin.org");
        httpProps.put("path", "/get");
        httpRequest.setProperties(httpProps);
        
        threadGroup.getChildren().add(httpRequest);
        
        ResultCollectorComponent resultTree = new ResultCollectorComponent();
        resultTree.setId(UUID.randomUUID().toString());
        resultTree.setName("察看结果树");
        resultTree.setParentId(threadGroup.getId());
        threadGroup.getChildren().add(resultTree);
        
        ScriptTemplate template = new ScriptTemplate();
        template.setName("http-basic");
        template.setDisplayName("HTTP基础测试");
        template.setDescription("包含线程组、HTTP请求和结果查看器的基础模板");
        template.setCategory("基础模板");
        template.setRootComponent(threadGroup);
        
        return template;
    }
    
    private ScriptTemplate createDataDrivenTemplate() {
        ThreadGroupComponent threadGroup = new ThreadGroupComponent();
        threadGroup.setId(UUID.randomUUID().toString());
        threadGroup.setName("数据驱动线程组");
        
        CsvDataSetComponent csvDataSet = new CsvDataSetComponent();
        csvDataSet.setId(UUID.randomUUID().toString());
        csvDataSet.setName("CSV数据集配置");
        csvDataSet.setParentId(threadGroup.getId());
        
        Map<String, Object> csvProps = csvDataSet.getDefaultProperties();
        csvProps.put("variableNames", "username,password");
        csvDataSet.setProperties(csvProps);
        threadGroup.getChildren().add(csvDataSet);
        
        ScriptTemplate template = new ScriptTemplate();
        template.setName("data-driven");
        template.setDisplayName("数据驱动测试");
        template.setDescription("使用CSV数据文件进行参数化测试的模板");
        template.setCategory("数据驱动");
        template.setRootComponent(threadGroup);
        
        return template;
    }
    
    private ScriptTemplate createApiTestTemplate() {
        ThreadGroupComponent threadGroup = new ThreadGroupComponent();
        threadGroup.setId(UUID.randomUUID().toString());
        threadGroup.setName("API测试线程组");
        
        HeaderManagerComponent headerManager = new HeaderManagerComponent();
        headerManager.setId(UUID.randomUUID().toString());
        headerManager.setName("HTTP信息头管理器");
        headerManager.setParentId(threadGroup.getId());
        threadGroup.getChildren().add(headerManager);
        
        ScriptTemplate template = new ScriptTemplate();
        template.setName("api-test");
        template.setDisplayName("API接口测试");
        template.setDescription("包含请求头、断言的完整API测试模板");
        template.setCategory("API测试");
        template.setRootComponent(threadGroup);
        
        return template;
    }
}
