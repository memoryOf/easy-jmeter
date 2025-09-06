package com.zhao.easyJmeter.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("scene")
public class SceneDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long moduleId;
    private String projectName;
    private String creator;
    private String createTime;
    private Integer status;
}