package com.musclegrow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 补剂详细信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("supplement_detail")
public class SupplementDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    //补剂id
    private Long supplementId;

    //补剂细节名称
    private String name;

    //补剂详细信息数据list
    private String value;

}
