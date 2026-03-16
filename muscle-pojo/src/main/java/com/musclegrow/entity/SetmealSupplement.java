package com.musclegrow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 套餐补剂关系
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("setmeal_supplement")
public class SetmealSupplement implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    //套餐id
    private Long setmealId;

    //补剂id
    private Long supplementId;

    //补剂名称 （冗余字段）
    private String name;

    //补剂单价（冗余字段）
    private BigDecimal price;

    //补剂份数
    private Integer copies;
}
