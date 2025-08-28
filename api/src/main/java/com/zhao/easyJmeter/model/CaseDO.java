package com.zhao.easyJmeter.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhao.easyJmeter.common.enumeration.JmeterStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@TableName("`case`")
@EqualsAndHashCode(callSuper = true)
public class CaseDO extends BaseModel implements Serializable {

    private static final long serialVersionUID = -1597190968205712177L;

    private String name;

    private Integer creator;

    private JmeterStatusEnum status;

    private Integer project;

    private String jmx;

    private String csv;

    private String jar;

    private String description;
}
