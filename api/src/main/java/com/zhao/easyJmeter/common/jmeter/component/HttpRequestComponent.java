package com.zhao.easyJmeter.common.jmeter.component;

import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.testelement.TestElement;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP请求组件实现
 */
public class HttpRequestComponent extends JMeterComponentBase {
    
    public HttpRequestComponent() {
        this.setComponentType("HTTPSamplerProxy");
        this.setMetadata(createMetadata());
        this.setProperties(getDefaultProperties());
    }
    
    @Override
    public String getTestElementClass() {
        return "org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy";
    }
    
    @Override
    public String getGuiClass() {
        return "org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui";
    }
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        
        // 验证服务器名称或IP
        Object domain = getProperties().get("domain");
        if (domain == null || !StringUtils.hasText(domain.toString())) {
            result.addError("domain", "服务器名称或IP不能为空");
        }
        
        // 验证路径
        Object path = getProperties().get("path");
        if (path == null || !StringUtils.hasText(path.toString())) {
            result.addWarning("path", "建议设置请求路径");
        }
        
        // 验证端口号
        Object port = getProperties().get("port");
        if (port != null && !port.toString().isEmpty()) {
            try {
                int portNum = Integer.parseInt(port.toString());
                if (portNum < 1 || portNum > 65535) {
                    result.addError("port", "端口号必须在1-65535范围内");
                }
            } catch (NumberFormatException e) {
                result.addError("port", "端口号必须是数字");
            }
        }
        
        return result;
    }
    
    @Override
    public TestElement toTestElement() {
        HTTPSamplerProxy httpSampler = new HTTPSamplerProxy();
        httpSampler.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
        httpSampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());
        httpSampler.setName(this.getName());
        httpSampler.setEnabled(this.isEnabled());
        
        // 设置基本属性
        Object domain = getProperties().get("domain");
        if (domain != null) {
            httpSampler.setDomain(domain.toString());
        }
        
        Object port = getProperties().get("port");
        if (port != null && !port.toString().isEmpty()) {
            httpSampler.setPort(Integer.parseInt(port.toString()));
        }
        
        Object protocol = getProperties().get("protocol");
        if (protocol != null) {
            httpSampler.setProtocol(protocol.toString());
        }
        
        Object method = getProperties().get("method");
        if (method != null) {
            httpSampler.setMethod(method.toString());
        }
        
        Object path = getProperties().get("path");
        if (path != null) {
            httpSampler.setPath(path.toString());
        }
        
        Object followRedirects = getProperties().get("followRedirects");
        if (followRedirects != null) {
            httpSampler.setFollowRedirects(Boolean.parseBoolean(followRedirects.toString()));
        }
        
        Object autoRedirects = getProperties().get("autoRedirects");
        if (autoRedirects != null) {
            httpSampler.setAutoRedirects(Boolean.parseBoolean(autoRedirects.toString()));
        }
        
        Object keepAlive = getProperties().get("keepAlive");
        if (keepAlive != null) {
            httpSampler.setUseKeepAlive(Boolean.parseBoolean(keepAlive.toString()));
        }
        
        Object multipart = getProperties().get("multipart");
        if (multipart != null) {
            httpSampler.setDoMultipart(Boolean.parseBoolean(multipart.toString()));
        }
        
        // 设置请求参数
        Object parameters = getProperties().get("parameters");
        if (parameters instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, String>> paramList = (List<Map<String, String>>) parameters;
            for (Map<String, String> param : paramList) {
                HTTPArgument httpArg = new HTTPArgument(param.get("name"), param.get("value"));
                httpArg.setAlwaysEncoded(Boolean.parseBoolean(param.getOrDefault("encode", "true")));
                httpSampler.getArguments().addArgument(httpArg);
            }
        }
        
        // 设置请求体
        Object postData = getProperties().get("postData");
        if (postData != null) {
            httpSampler.addNonEncodedArgument("", postData.toString(), "");
        }
        
        return httpSampler;
    }
    
    @Override
    public void fromTestElement(TestElement element) {
        if (!(element instanceof HTTPSamplerProxy)) {
            throw new IllegalArgumentException("Element must be a HTTPSamplerProxy");
        }
        
        HTTPSamplerProxy httpSampler = (HTTPSamplerProxy) element;
        this.setName(httpSampler.getName());
        this.setEnabled(httpSampler.isEnabled());
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("domain", httpSampler.getDomain());
        properties.put("port", httpSampler.getPort());
        properties.put("protocol", httpSampler.getProtocol());
        properties.put("method", httpSampler.getMethod());
        properties.put("path", httpSampler.getPath());
        properties.put("followRedirects", httpSampler.getFollowRedirects());
        properties.put("autoRedirects", httpSampler.getAutoRedirects());
        properties.put("keepAlive", httpSampler.getUseKeepAlive());
        properties.put("multipart", httpSampler.getDoMultipart());
        
        // 提取请求参数
        List<Map<String, String>> parameters = new ArrayList<>();
        for (int i = 0; i < httpSampler.getArguments().getArgumentCount(); i++) {
            HTTPArgument arg = (HTTPArgument) httpSampler.getArguments().getArgument(i);
            Map<String, String> param = new HashMap<>();
            param.put("name", arg.getName());
            param.put("value", arg.getValue());
            param.put("encode", String.valueOf(arg.isAlwaysEncoded()));
            parameters.add(param);
        }
        properties.put("parameters", parameters);
        
        this.setProperties(properties);
    }
    
    @Override
    public Map<String, Object> getDefaultProperties() {
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("domain", "");
        defaults.put("port", "");
        defaults.put("protocol", "http");
        defaults.put("method", "GET");
        defaults.put("path", "");
        defaults.put("followRedirects", true);
        defaults.put("autoRedirects", false);
        defaults.put("keepAlive", true);
        defaults.put("multipart", false);
        defaults.put("parameters", new ArrayList<>());
        defaults.put("postData", "");
        return defaults;
    }
    
    @Override
    public List<PropertyDefinition> getSupportedProperties() {
        List<PropertyDefinition> properties = new ArrayList<>();
        
        // 基本设置
        properties.add(PropertyDefinition.builder()
                .name("domain")
                .displayName("服务器名称或IP")
                .description("目标服务器的域名或IP地址")
                .type(PropertyDefinition.PropertyType.STRING)
                .defaultValue("")
                .required(true)
                .group("基本设置")
                .order(1)
                .build());
                
        properties.add(PropertyDefinition.builder()
                .name("port")
                .displayName("端口号")
                .description("目标服务器的端口号")
                .type(PropertyDefinition.PropertyType.STRING)
                .defaultValue("")
                .group("基本设置")
                .order(2)
                .build());
                
        properties.add(PropertyDefinition.builder()
                .name("protocol")
                .displayName("协议")
                .description("请求协议")
                .type(PropertyDefinition.PropertyType.SELECT)
                .defaultValue("http")
                .options(List.of(
                    PropertyDefinition.SelectOption.builder().label("HTTP").value("http").build(),
                    PropertyDefinition.SelectOption.builder().label("HTTPS").value("https").build()
                ))
                .group("基本设置")
                .order(3)
                .build());
                
        properties.add(PropertyDefinition.builder()
                .name("method")
                .displayName("请求方法")
                .description("HTTP请求方法")
                .type(PropertyDefinition.PropertyType.SELECT)
                .defaultValue("GET")
                .options(List.of(
                    PropertyDefinition.SelectOption.builder().label("GET").value("GET").build(),
                    PropertyDefinition.SelectOption.builder().label("POST").value("POST").build(),
                    PropertyDefinition.SelectOption.builder().label("PUT").value("PUT").build(),
                    PropertyDefinition.SelectOption.builder().label("DELETE").value("DELETE").build(),
                    PropertyDefinition.SelectOption.builder().label("HEAD").value("HEAD").build(),
                    PropertyDefinition.SelectOption.builder().label("OPTIONS").value("OPTIONS").build(),
                    PropertyDefinition.SelectOption.builder().label("PATCH").value("PATCH").build()
                ))
                .group("基本设置")
                .order(4)
                .build());
                
        properties.add(PropertyDefinition.builder()
                .name("path")
                .displayName("路径")
                .description("请求路径")
                .type(PropertyDefinition.PropertyType.STRING)
                .defaultValue("")
                .group("基本设置")
                .order(5)
                .build());
        
        // 高级设置
        properties.add(PropertyDefinition.builder()
                .name("followRedirects")
                .displayName("跟随重定向")
                .description("是否自动跟随重定向")
                .type(PropertyDefinition.PropertyType.BOOLEAN)
                .defaultValue(true)
                .group("高级设置")
                .order(6)
                .build());
                
        properties.add(PropertyDefinition.builder()
                .name("keepAlive")
                .displayName("保持连接")
                .description("是否使用Keep-Alive")
                .type(PropertyDefinition.PropertyType.BOOLEAN)
                .defaultValue(true)
                .group("高级设置")
                .order(7)
                .build());
                
        properties.add(PropertyDefinition.builder()
                .name("multipart")
                .displayName("使用multipart/form-data")
                .description("是否使用multipart/form-data编码")
                .type(PropertyDefinition.PropertyType.BOOLEAN)
                .defaultValue(false)
                .group("高级设置")
                .order(8)
                .build());
        
        return properties;
    }
    
    private ComponentMetadata createMetadata() {
        return ComponentMetadata.builder()
                .displayName("HTTP请求")
                .description("发送HTTP请求到指定的服务器")
                .category("取样器")
                .icon("icon-http-request")
                .container(false)
                .acceptedChildren(new String[]{})
                .acceptedParents(new String[]{"ThreadGroup"})
                .sortOrder(10)
                .builtin(true)
                .build();
    }
}
