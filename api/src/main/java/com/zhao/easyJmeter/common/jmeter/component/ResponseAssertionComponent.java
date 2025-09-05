package com.zhao.easyJmeter.common.jmeter.component;

import org.apache.jmeter.assertions.ResponseAssertion;
import org.apache.jmeter.assertions.gui.AssertionGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 响应断言组件实现
 */
public class ResponseAssertionComponent extends JMeterComponentBase {

    public ResponseAssertionComponent() {
        this.setComponentType("ResponseAssertion");
        this.setMetadata(createMetadata());
        this.setProperties(getDefaultProperties());
    }

    @Override
    public String getTestElementClass() {
        return "org.apache.jmeter.assertions.ResponseAssertion";
    }

    @Override
    public String getGuiClass() {
        return "org.apache.jmeter.assertions.gui.AssertionGui";
    }

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();

        // 验证断言规则
        Object testStrings = getProperties().get("testStrings");
        if (testStrings instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> patterns = (List<String>) testStrings;
            if (patterns.isEmpty()) {
                result.addWarning("testStrings", "建议添加至少一个断言规则");
            }

            // 验证每个规则不为空
            for (int i = 0; i < patterns.size(); i++) {
                if (!StringUtils.hasText(patterns.get(i))) {
                    result.addError("testStrings[" + i + "]", "断言规则不能为空");
                }
            }
        }

        return result;
    }

    @Override
    public TestElement toTestElement() {
        ResponseAssertion assertion = new ResponseAssertion();
        assertion.setProperty(TestElement.TEST_CLASS, ResponseAssertion.class.getName());
        assertion.setProperty(TestElement.GUI_CLASS, AssertionGui.class.getName());
        assertion.setName(this.getName());
        assertion.setEnabled(this.isEnabled());

        // 设置测试字段
        Object testField = getProperties().get("testField");
        if (testField != null) {
            assertion.setTestFieldResponseData();
            String field = testField.toString();
            switch (field) {
                case "RESPONSE_DATA":
                    assertion.setTestFieldResponseData();
                    break;
                case "RESPONSE_CODE":
                    assertion.setTestFieldResponseCode();
                    break;
                case "RESPONSE_MESSAGE":
                    assertion.setTestFieldResponseMessage();
                    break;
                case "RESPONSE_HEADERS":
                    assertion.setTestFieldResponseHeaders();
                    break;
                case "REQUEST_HEADERS":
                    assertion.setTestFieldRequestHeaders();
                    break;
                case "REQUEST_DATA":
                    assertion.setTestFieldRequestData();
                    break;
                case "URL":
                    assertion.setTestFieldURL();
                    break;
            }
        }

        // 设置匹配规则
        Object matchRule = getProperties().get("matchRule");
        if (matchRule != null) {
            String rule = matchRule.toString();
            switch (rule) {
                case "CONTAINS":
                    assertion.setToContainsType();
                    break;
                case "MATCHES":
                    assertion.setToMatchType();
                    break;
                case "EQUALS":
                    assertion.setToEqualsType();
                    break;
                case "SUBSTRING":
                    assertion.setToSubstringType();
                    break;
                case "NOT_CONTAINS":
                    assertion.setToNotType();
                    assertion.setToContainsType();
                    break;
                case "NOT_MATCHES":
                    assertion.setToNotType();
                    assertion.setToMatchType();
                    break;
                case "NOT_EQUALS":
                    assertion.setToNotType();
                    assertion.setToEqualsType();
                    break;
                case "NOT_SUBSTRING":
                    assertion.setToNotType();
                    assertion.setToSubstringType();
                    break;
            }
        }

        // 设置断言模式
        Object assumeSuccess = getProperties().get("assumeSuccess");
        if (assumeSuccess != null) {
            assertion.setAssumeSuccess(Boolean.parseBoolean(assumeSuccess.toString()));
        }

        // 设置测试字符串
        Object testStrings = getProperties().get("testStrings");
        if (testStrings instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> patterns = (List<String>) testStrings;
            for (String pattern : patterns) {
                if (StringUtils.hasText(pattern)) {
                    assertion.addTestString(pattern);
                }
            }
        }

        return assertion;
    }

    @Override
    public void fromTestElement(TestElement element) {
        if (!(element instanceof ResponseAssertion)) {
            throw new IllegalArgumentException("Element must be a ResponseAssertion");
        }

        ResponseAssertion assertion = (ResponseAssertion) element;
        this.setName(assertion.getName());
        this.setEnabled(assertion.isEnabled());

        Map<String, Object> properties = new HashMap<>();

        // 获取测试字段
        String testField = "RESPONSE_DATA";
        if (assertion.isTestFieldResponseCode()) {
            testField = "RESPONSE_CODE";
        } else if (assertion.isTestFieldResponseMessage()) {
            testField = "RESPONSE_MESSAGE";
        } else if (assertion.isTestFieldResponseHeaders()) {
            testField = "RESPONSE_HEADERS";
        } else if (assertion.isTestFieldRequestHeaders()) {
            testField = "REQUEST_HEADERS";
        } else if (assertion.isTestFieldRequestData()) {
            testField = "REQUEST_DATA";
        } else if (assertion.isTestFieldURL()) {
            testField = "URL";
        }
        properties.put("testField", testField);

        // 获取匹配规则
        String matchRule = "CONTAINS";
        boolean isNot = assertion.isNotType();
        if (assertion.isContainsType()) {
            matchRule = isNot ? "NOT_CONTAINS" : "CONTAINS";
        } else if (assertion.isMatchType()) {
            matchRule = isNot ? "NOT_MATCHES" : "MATCHES";
        } else if (assertion.isEqualsType()) {
            matchRule = isNot ? "NOT_EQUALS" : "EQUALS";
        } else if (assertion.isSubstringType()) {
            matchRule = isNot ? "NOT_SUBSTRING" : "SUBSTRING";
        }
        properties.put("matchRule", matchRule);

        properties.put("assumeSuccess", assertion.getAssumeSuccess());

        // 获取测试字符串
        List<String> testStrings = new ArrayList<>();
        CollectionProperty testStringArray = assertion.getTestStrings();
        if (testStringArray != null) {
            for (JMeterProperty testString : testStringArray) {
                testStrings.add(String.valueOf(testString));
            }
        }
        properties.put("testStrings", testStrings);

        this.setProperties(properties);
    }

    @Override
    public Map<String, Object> getDefaultProperties() {
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("testField", "RESPONSE_DATA");
        defaults.put("matchRule", "CONTAINS");
        defaults.put("assumeSuccess", false);
        defaults.put("testStrings", List.of("success"));
        return defaults;
    }

    @Override
    public List<PropertyDefinition> getSupportedProperties() {
        List<PropertyDefinition> properties = new ArrayList<>();

        properties.add(PropertyDefinition.builder()
                .name("testField")
                .displayName("响应字段")
                .description("要测试的响应字段")
                .type(PropertyDefinition.PropertyType.SELECT)
                .defaultValue("RESPONSE_DATA")
                .options(List.of(
                        PropertyDefinition.SelectOption.builder().label("响应文本").value("RESPONSE_DATA").build(),
                        PropertyDefinition.SelectOption.builder().label("响应代码").value("RESPONSE_CODE").build(),
                        PropertyDefinition.SelectOption.builder().label("响应信息").value("RESPONSE_MESSAGE").build(),
                        PropertyDefinition.SelectOption.builder().label("响应头").value("RESPONSE_HEADERS").build(),
                        PropertyDefinition.SelectOption.builder().label("请求头").value("REQUEST_HEADERS").build(),
                        PropertyDefinition.SelectOption.builder().label("请求数据").value("REQUEST_DATA").build(),
                        PropertyDefinition.SelectOption.builder().label("URL样本").value("URL").build()
                ))
                .group("基本设置")
                .order(1)
                .build());

        properties.add(PropertyDefinition.builder()
                .name("matchRule")
                .displayName("模式匹配规则")
                .description("断言的匹配规则")
                .type(PropertyDefinition.PropertyType.SELECT)
                .defaultValue("CONTAINS")
                .options(List.of(
                        PropertyDefinition.SelectOption.builder().label("包含").value("CONTAINS").build(),
                        PropertyDefinition.SelectOption.builder().label("匹配").value("MATCHES").build(),
                        PropertyDefinition.SelectOption.builder().label("等于").value("EQUALS").build(),
                        PropertyDefinition.SelectOption.builder().label("子串").value("SUBSTRING").build(),
                        PropertyDefinition.SelectOption.builder().label("不包含").value("NOT_CONTAINS").build(),
                        PropertyDefinition.SelectOption.builder().label("不匹配").value("NOT_MATCHES").build(),
                        PropertyDefinition.SelectOption.builder().label("不等于").value("NOT_EQUALS").build(),
                        PropertyDefinition.SelectOption.builder().label("非子串").value("NOT_SUBSTRING").build()
                ))
                .group("基本设置")
                .order(2)
                .build());

        properties.add(PropertyDefinition.builder()
                .name("assumeSuccess")
                .displayName("忽略状态")
                .description("忽略状态码，将所有响应视为成功")
                .type(PropertyDefinition.PropertyType.BOOLEAN)
                .defaultValue(false)
                .group("高级设置")
                .order(3)
                .build());

        properties.add(PropertyDefinition.builder()
                .name("testStrings")
                .displayName("要测试的模式")
                .description("断言的模式列表")
                .type(PropertyDefinition.PropertyType.JSON)
                .defaultValue(List.of("success"))
                .group("模式设置")
                .order(4)
                .build());

        return properties;
    }

    private ComponentMetadata createMetadata() {
        return ComponentMetadata.builder()
                .displayName("响应断言")
                .description("对响应结果进行断言验证")
                .category("断言")
                .icon("icon-response-assertion")
                .container(false)
                .acceptedChildren(new String[]{})
                .acceptedParents(new String[]{"ThreadGroup", "HTTPSamplerProxy"})
                .sortOrder(40)
                .builtin(true)
                .build();
    }
}
