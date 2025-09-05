package com.zhao.easyJmeter.common.jmeter.component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 组件属性定义
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyDefinition {
    
    /**
     * 属性名称
     */
    private String name;
    
    /**
     * 属性显示名称
     */
    private String displayName;
    
    /**
     * 属性描述
     */
    private String description;
    
    /**
     * 属性类型
     */
    private PropertyType type;
    
    /**
     * 默认值
     */
    private Object defaultValue;
    
    /**
     * 是否必填
     */
    private boolean required;
    
    /**
     * 是否只读
     */
    private boolean readonly;
    
    /**
     * 属性值验证规则
     */
    private List<ValidationRule> validationRules;
    
    /**
     * 可选值列表（用于枚举类型）
     */
    private List<SelectOption> options;
    
    /**
     * 属性分组
     */
    private String group;
    
    /**
     * 属性排序
     */
    private int order;
    
    /**
     * 属性提示信息
     */
    private String placeholder;
    
    /**
     * 是否在UI中显示
     */
    private boolean visible = true;
    
    /**
     * 属性类型枚举
     */
    public enum PropertyType {
        STRING,        // 字符串
        INTEGER,       // 整数
        LONG,         // 长整数
        DOUBLE,       // 双精度浮点数
        BOOLEAN,      // 布尔值
        SELECT,       // 下拉选择
        MULTISELECT,  // 多选
        TEXTAREA,     // 文本域
        PASSWORD,     // 密码
        FILE,         // 文件
        JSON,         // JSON对象
        EXPRESSION,   // 表达式
        DURATION      // 时间间隔
    }
    
    /**
     * 选项定义
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SelectOption {
        private String label;
        private Object value;
        private String description;
    }
    
    /**
     * 验证规则
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationRule {
        private String type;        // 规则类型：required, min, max, pattern, etc.
        private Object value;       // 规则值
        private String message;     // 错误信息
    }
}
