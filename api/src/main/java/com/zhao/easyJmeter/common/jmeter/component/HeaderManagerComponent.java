package com.zhao.easyJmeter.common.jmeter.component;

import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.gui.HeaderPanel;
import org.apache.jmeter.testelement.TestElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP信息头管理器组件实现
 */
public class HeaderManagerComponent extends JMeterComponentBase {

    public HeaderManagerComponent() {
        this.setComponentType("HeaderManager");
        this.setMetadata(createMetadata());
        this.setProperties(getDefaultProperties());
    }

    @Override
    public String getTestElementClass() {
        return "org.apache.jmeter.protocol.http.control.HeaderManager";
    }

    @Override
    public String getGuiClass() {
        return "org.apache.jmeter.protocol.http.gui.HeaderPanel";
    }

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();

        // 验证头信息列表
        Object headers = getProperties().get("headers");
        if (headers instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, String>> headerList = (List<Map<String, String>>) headers;
            for (int i = 0; i < headerList.size(); i++) {
                Map<String, String> header = headerList.get(i);
                String name = header.get("name");
                if (name == null || name.trim().isEmpty()) {
                    result.addError("headers[" + i + "].name", "头信息名称不能为空");
                }
                // 检查重复的头信息
                for (int j = i + 1; j < headerList.size(); j++) {
                    if (name != null && name.equals(headerList.get(j).get("name"))) {
                        result.addWarning("headers[" + i + "].name", "存在重复的头信息名称: " + name);
                        break;
                    }
                }
            }
        }

        return result;
    }

    @Override
    public TestElement toTestElement() {
        HeaderManager headerManager = new HeaderManager();
        headerManager.setProperty(TestElement.TEST_CLASS, HeaderManager.class.getName());
        headerManager.setProperty(TestElement.GUI_CLASS, HeaderPanel.class.getName());
        headerManager.setName(this.getName());
        headerManager.setEnabled(this.isEnabled());

        // 设置头信息列表
        Object headers = getProperties().get("headers");
        if (headers instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, String>> headerList = (List<Map<String, String>>) headers;
            for (Map<String, String> headerMap : headerList) {
                String name = headerMap.get("name");
                String value = headerMap.get("value");
                if (name != null && !name.trim().isEmpty()) {
                    Header header = new Header(name, value != null ? value : "");
                    headerManager.add(header);
                }
            }
        }

        return headerManager;
    }

    @Override
    public void fromTestElement(TestElement element) {
        if (!(element instanceof HeaderManager)) {
            throw new IllegalArgumentException("Element must be a HeaderManager");
        }

        HeaderManager headerManager = (HeaderManager) element;
        this.setName(headerManager.getName());
        this.setEnabled(headerManager.isEnabled());

        Map<String, Object> properties = new HashMap<>();

        // 提取头信息列表
        List<Map<String, String>> headers = new ArrayList<>();
        for (int i = 0; i < headerManager.getHeaders().size(); i++) {
            Header header = headerManager.getHeader(i);
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("name", header.getName());
            headerMap.put("value", header.getValue());
            headers.add(headerMap);
        }
        properties.put("headers", headers);

        this.setProperties(properties);
    }

    @Override
    public Map<String, Object> getDefaultProperties() {
        Map<String, Object> defaults = new HashMap<>();

        // 设置一些常用的默认头信息
        List<Map<String, String>> defaultHeaders = new ArrayList<>();

        // Content-Type
        Map<String, String> contentType = new HashMap<>();
        contentType.put("name", "Content-Type");
        contentType.put("value", "application/json");
        defaultHeaders.add(contentType);

        // User-Agent
        Map<String, String> userAgent = new HashMap<>();
        userAgent.put("name", "User-Agent");
        userAgent.put("value", "Mozilla/5.0 (compatible; Easy-JMeter)");
        defaultHeaders.add(userAgent);

        defaults.put("headers", defaultHeaders);
        return defaults;
    }

    @Override
    public List<PropertyDefinition> getSupportedProperties() {
        List<PropertyDefinition> properties = new ArrayList<>();

        properties.add(PropertyDefinition.builder()
                .name("headers")
                .displayName("HTTP头信息")
                .description("HTTP请求头信息列表")
                .type(PropertyDefinition.PropertyType.JSON)
                .defaultValue(getDefaultProperties().get("headers"))
                .group("头信息设置")
                .order(1)
                .build());

        return properties;
    }

    private ComponentMetadata createMetadata() {
        return ComponentMetadata.builder()
                .displayName("HTTP信息头管理器")
                .description("管理HTTP请求的头信息")
                .category("配置元素")
                .icon("icon-header-manager")
                .container(false)
                .acceptedChildren(new String[]{})
                .acceptedParents(new String[]{"ThreadGroup", "HTTPSamplerProxy"})
                .sortOrder(20)
                .builtin(true)
                .build();
    }
}
