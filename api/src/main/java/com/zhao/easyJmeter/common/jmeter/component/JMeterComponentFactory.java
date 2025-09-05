package com.zhao.easyJmeter.common.jmeter.component;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * JMeter组件工厂类
 * 负责创建和管理各种JMeter组件实例
 */
@Component
public class JMeterComponentFactory {
    
    private final Map<String, Class<? extends JMeterComponentBase>> componentRegistry;
    private final Map<String, ComponentMetadata> metadataCache;
    
    public JMeterComponentFactory() {
        this.componentRegistry = new HashMap<>();
        this.metadataCache = new HashMap<>();
        initializeComponents();
    }
    
    /**
     * 初始化内置组件
     */
    private void initializeComponents() {
        // 注册内置组件
        registerComponent("ThreadGroup", ThreadGroupComponent.class);
        registerComponent("HTTPSamplerProxy", HttpRequestComponent.class);
        registerComponent("HeaderManager", HeaderManagerComponent.class);
        registerComponent("CSVDataSet", CsvDataSetComponent.class);
        registerComponent("ResponseAssertion", ResponseAssertionComponent.class);
        registerComponent("ResultCollector", ResultCollectorComponent.class);
        
        // 预加载元数据缓存
        loadMetadataCache();
    }
    
    /**
     * 注册组件类型
     */
    public void registerComponent(String componentType, Class<? extends JMeterComponentBase> componentClass) {
        componentRegistry.put(componentType, componentClass);
    }
    
    /**
     * 创建组件实例
     */
    public JMeterComponentBase createComponent(String componentType) {
        Class<? extends JMeterComponentBase> componentClass = componentRegistry.get(componentType);
        if (componentClass == null) {
            throw new IllegalArgumentException("Unknown component type: " + componentType);
        }
        
        try {
            return componentClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create component instance: " + componentType, e);
        }
    }
    
    /**
     * 创建带配置的组件实例
     */
    public JMeterComponentBase createComponent(String componentType, String name, Map<String, Object> properties) {
        JMeterComponentBase component = createComponent(componentType);
        component.setId(UUID.randomUUID().toString());
        component.setName(name);
        
        if (properties != null) {
            // 合并默认属性和传入属性
            Map<String, Object> mergedProperties = new HashMap<>(component.getDefaultProperties());
            mergedProperties.putAll(properties);
            component.setProperties(mergedProperties);
        }
        
        return component;
    }
    
    /**
     * 获取所有支持的组件类型
     */
    public Set<String> getSupportedComponentTypes() {
        return new HashSet<>(componentRegistry.keySet());
    }
    
    /**
     * 获取组件元数据
     */
    public ComponentMetadata getComponentMetadata(String componentType) {
        return metadataCache.get(componentType);
    }
    
    /**
     * 获取所有组件元数据
     */
    public List<ComponentMetadata> getAllComponentMetadata() {
        return new ArrayList<>(metadataCache.values());
    }
    
    /**
     * 按类别获取组件元数据
     */
    public Map<String, List<ComponentMetadata>> getComponentMetadataByCategory() {
        return metadataCache.values().stream()
                .collect(Collectors.groupingBy(ComponentMetadata::getCategory));
    }
    
    /**
     * 获取组件支持的属性定义
     */
    public List<PropertyDefinition> getComponentPropertyDefinitions(String componentType) {
        JMeterComponentBase component = createComponent(componentType);
        return component.getSupportedProperties();
    }
    
    /**
     * 验证组件配置
     */
    public ValidationResult validateComponent(JMeterComponentBase component) {
        if (component == null) {
            return ValidationResult.failure("component", "组件不能为空");
        }
        
        return component.validate();
    }
    
    /**
     * 检查组件是否为容器类型
     */
    public boolean isContainerComponent(String componentType) {
        ComponentMetadata metadata = getComponentMetadata(componentType);
        return metadata != null && metadata.isContainer();
    }
    
    /**
     * 检查父子组件关系是否有效
     */
    public boolean isValidParentChild(String parentType, String childType) {
        ComponentMetadata childMetadata = getComponentMetadata(childType);
        if (childMetadata == null || childMetadata.getAcceptedParents() == null) {
            return false;
        }
        
        return Arrays.asList(childMetadata.getAcceptedParents()).contains(parentType);
    }
    
    /**
     * 获取组件可接受的子组件类型
     */
    public List<String> getAcceptedChildren(String componentType) {
        ComponentMetadata metadata = getComponentMetadata(componentType);
        if (metadata == null || metadata.getAcceptedChildren() == null) {
            return Collections.emptyList();
        }
        
        return Arrays.asList(metadata.getAcceptedChildren());
    }
    
    /**
     * 创建组件树
     */
    public JMeterComponentBase createComponentTree(ComponentTreeNode treeNode) {
        if (treeNode == null) {
            return null;
        }
        
        // 创建根组件
        JMeterComponentBase rootComponent = createComponent(
                treeNode.getComponentType(),
                treeNode.getName(),
                treeNode.getProperties()
        );
        
        // 递归创建子组件
        if (treeNode.getChildren() != null) {
            for (ComponentTreeNode childNode : treeNode.getChildren()) {
                JMeterComponentBase childComponent = createComponentTree(childNode);
                if (childComponent != null) {
                    childComponent.setParentId(rootComponent.getId());
                    childComponent.setOrder(rootComponent.getChildren().size());
                    rootComponent.getChildren().add(childComponent);
                }
            }
        }
        
        return rootComponent;
    }
    
    /**
     * 加载元数据缓存
     */
    private void loadMetadataCache() {
        for (String componentType : componentRegistry.keySet()) {
            try {
                JMeterComponentBase component = createComponent(componentType);
                metadataCache.put(componentType, component.getMetadata());
            } catch (Exception e) {
                // 记录日志但不中断启动过程
                System.err.println("Failed to load metadata for component: " + componentType + ", " + e.getMessage());
            }
        }
    }
    
    /**
     * 组件树节点定义
     */
    public static class ComponentTreeNode {
        private String componentType;
        private String name;
        private Map<String, Object> properties;
        private List<ComponentTreeNode> children;
        
        // Getters and setters
        public String getComponentType() { return componentType; }
        public void setComponentType(String componentType) { this.componentType = componentType; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Map<String, Object> getProperties() { return properties; }
        public void setProperties(Map<String, Object> properties) { this.properties = properties; }
        
        public List<ComponentTreeNode> getChildren() { return children; }
        public void setChildren(List<ComponentTreeNode> children) { this.children = children; }
    }
}
