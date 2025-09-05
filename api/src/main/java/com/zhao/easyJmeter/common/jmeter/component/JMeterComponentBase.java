package com.zhao.easyJmeter.common.jmeter.component;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JMeter组件抽象基类
 * 参考银行90%组件重写的设计理念，提供统一的组件抽象
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "componentType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ThreadGroupComponent.class, name = "ThreadGroup"),
    @JsonSubTypes.Type(value = HttpRequestComponent.class, name = "HTTPSamplerProxy"),
    @JsonSubTypes.Type(value = HeaderManagerComponent.class, name = "HeaderManager"),
    @JsonSubTypes.Type(value = CsvDataSetComponent.class, name = "CSVDataSet"),
    @JsonSubTypes.Type(value = ResponseAssertionComponent.class, name = "ResponseAssertion"),
    @JsonSubTypes.Type(value = ResultCollectorComponent.class, name = "ResultCollector")
})
public abstract class JMeterComponentBase {
    
    /**
     * 组件唯一标识
     */
    private String id;
    
    /**
     * 组件名称
     */
    private String name;
    
    /**
     * 组件类型
     */
    private String componentType;
    
    /**
     * 是否启用
     */
    private boolean enabled = true;
    
    /**
     * 父组件ID
     */
    private String parentId;
    
    /**
     * 子组件列表
     */
    private List<JMeterComponentBase> children = new ArrayList<>();
    
    /**
     * 组件属性配置
     */
    private Map<String, Object> properties = new HashMap<>();
    
    /**
     * 组件在树中的位置
     */
    private int order = 0;
    
    /**
     * 组件描述信息
     */
    private ComponentMetadata metadata;
    
    /**
     * 获取组件的JMeter TestElement类名
     */
    public abstract String getTestElementClass();
    
    /**
     * 获取组件的GUI类名
     */
    public abstract String getGuiClass();
    
    /**
     * 验证组件配置是否有效
     */
    public abstract ValidationResult validate();
    
    /**
     * 将组件转换为JMeter TestElement
     */
    public abstract org.apache.jmeter.testelement.TestElement toTestElement();
    
    /**
     * 从JMeter TestElement构建组件
     */
    public abstract void fromTestElement(org.apache.jmeter.testelement.TestElement element);
    
    /**
     * 获取组件默认属性配置
     */
    public abstract Map<String, Object> getDefaultProperties();
    
    /**
     * 获取组件支持的属性定义
     */
    public abstract List<PropertyDefinition> getSupportedProperties();
}
