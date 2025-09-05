package com.zhao.easyJmeter.common.jmeter.component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 组件元数据信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComponentMetadata {
    
    /**
     * 组件显示名称
     */
    private String displayName;
    
    /**
     * 组件描述
     */
    private String description;
    
    /**
     * 组件类别
     */
    private String category;
    
    /**
     * 组件图标
     */
    private String icon;
    
    /**
     * 是否为容器组件（可包含子组件）
     */
    private boolean container;
    
    /**
     * 可接受的子组件类型
     */
    private String[] acceptedChildren;
    
    /**
     * 可接受的父组件类型
     */
    private String[] acceptedParents;
    
    /**
     * 组件排序权重
     */
    private int sortOrder;
    
    /**
     * 是否为系统内置组件
     */
    private boolean builtin = true;
}
