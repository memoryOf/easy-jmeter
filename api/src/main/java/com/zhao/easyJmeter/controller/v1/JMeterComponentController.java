package com.zhao.easyJmeter.controller.v1;

import com.zhao.easyJmeter.common.jmeter.component.*;
import com.zhao.easyJmeter.service.JMeterScriptService;
import com.zhao.easyJmeter.vo.CreatedVO;
import com.zhao.easyJmeter.vo.DeletedVO;
import com.zhao.easyJmeter.vo.PageResponseVO;
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
 * JMeter组件管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/jmeter/component")
@Api(tags = "JMeter组件管理")
@RequiredArgsConstructor
@Validated
public class JMeterComponentController {
    
    private final JMeterComponentFactory componentFactory;
    private final JMeterScriptService scriptService;
    
    @GetMapping("/metadata")
    @ApiOperation(value = "获取所有组件元数据", notes = "获取平台支持的所有JMeter组件的元数据信息")
    @LoginRequired
    public Map<String, List<ComponentMetadata>> getComponentMetadata() {
        return componentFactory.getComponentMetadataByCategory();
    }
    
    @GetMapping("/metadata/{componentType}")
    @ApiOperation(value = "获取指定组件元数据", notes = "根据组件类型获取元数据信息")
    @LoginRequired
    public ComponentMetadata getComponentMetadata(@PathVariable String componentType) {
        ComponentMetadata metadata = componentFactory.getComponentMetadata(componentType);
        if (metadata == null) {
            throw new IllegalArgumentException("不支持的组件类型: " + componentType);
        }
        return metadata;
    }
    
    @GetMapping("/properties/{componentType}")
    @ApiOperation(value = "获取组件属性定义", notes = "获取指定组件类型支持的属性定义")
    @LoginRequired
    public List<PropertyDefinition> getComponentProperties(@PathVariable String componentType) {
        return componentFactory.getComponentPropertyDefinitions(componentType);
    }
    
    @PostMapping("/create")
    @ApiOperation(value = "创建组件实例", notes = "根据组件类型和配置创建组件实例")
    @LoginRequired
    public JMeterComponentBase createComponent(@RequestBody CreateComponentRequest request) {
        return componentFactory.createComponent(
                request.getComponentType(),
                request.getName(),
                request.getProperties()
        );
    }
    
    @PostMapping("/validate")
    @ApiOperation(value = "验证组件配置", notes = "验证组件配置的有效性")
    @LoginRequired
    public ValidationResult validateComponent(@RequestBody JMeterComponentBase component) {
        return componentFactory.validateComponent(component);
    }
    
    @PostMapping("/tree/validate")
    @ApiOperation(value = "验证组件树", notes = "验证整个组件树的有效性")
    @LoginRequired
    public ValidationResult validateComponentTree(@RequestBody JMeterComponentBase rootComponent) {
        return scriptService.validateComponentTree(rootComponent);
    }
    
    @GetMapping("/relationship/{parentType}/{childType}")
    @ApiOperation(value = "检查组件关系", notes = "检查两个组件类型之间是否存在有效的父子关系")
    @LoginRequired
    public boolean checkComponentRelationship(
            @PathVariable String parentType,
            @PathVariable String childType) {
        return componentFactory.isValidParentChild(parentType, childType);
    }
    
    @GetMapping("/children/{componentType}")
    @ApiOperation(value = "获取可接受的子组件", notes = "获取指定组件类型可以接受的子组件类型列表")
    @LoginRequired
    public List<String> getAcceptedChildren(@PathVariable String componentType) {
        return componentFactory.getAcceptedChildren(componentType);
    }
    
    @PostMapping("/tree/create")
    @ApiOperation(value = "创建组件树", notes = "根据组件树定义创建完整的组件树")
    @LoginRequired
    public JMeterComponentBase createComponentTree(@RequestBody JMeterComponentFactory.ComponentTreeNode treeNode) {
        return componentFactory.createComponentTree(treeNode);
    }
    
    /**
     * 创建组件请求DTO
     */
    public static class CreateComponentRequest {
        private String componentType;
        private String name;
        private Map<String, Object> properties;
        
        // Getters and setters
        public String getComponentType() { return componentType; }
        public void setComponentType(String componentType) { this.componentType = componentType; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Map<String, Object> getProperties() { return properties; }
        public void setProperties(Map<String, Object> properties) { this.properties = properties; }
    }
}
