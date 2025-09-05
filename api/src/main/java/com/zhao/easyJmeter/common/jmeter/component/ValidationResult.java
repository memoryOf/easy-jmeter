package com.zhao.easyJmeter.common.jmeter.component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 验证结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResult {
    
    /**
     * 是否验证通过
     */
    private boolean valid = true;
    
    /**
     * 错误信息列表
     */
    @Builder.Default
    private List<ValidationError> errors = new ArrayList<>();
    
    /**
     * 警告信息列表
     */
    @Builder.Default
    private List<ValidationWarning> warnings = new ArrayList<>();
    
    /**
     * 添加错误信息
     */
    public void addError(String field, String message) {
        this.valid = false;
        this.errors.add(ValidationError.builder()
                .field(field)
                .message(message)
                .build());
    }
    
    /**
     * 添加警告信息
     */
    public void addWarning(String field, String message) {
        this.warnings.add(ValidationWarning.builder()
                .field(field)
                .message(message)
                .build());
    }
    
    /**
     * 创建成功的验证结果
     */
    public static ValidationResult success() {
        return ValidationResult.builder()
                .valid(true)
                .build();
    }
    
    /**
     * 创建失败的验证结果
     */
    public static ValidationResult failure(String field, String message) {
        ValidationResult result = new ValidationResult();
        result.addError(field, message);
        return result;
    }
    
    /**
     * 验证错误信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        private String field;
        private String message;
        private String code;
    }
    
    /**
     * 验证警告信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationWarning {
        private String field;
        private String message;
        private String code;
    }
}
