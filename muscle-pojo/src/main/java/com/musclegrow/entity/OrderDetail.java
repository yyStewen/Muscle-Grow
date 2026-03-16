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
 * 订单明细
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("order_detail")
public class OrderDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    //名称
    private String name;

    //图片
    private String image;

    //订单id
    private Long orderId;

    //补剂id
    private Long supplementId;

    //套餐id
    private Long setmealId;

    //关联的优惠券id
    private Long voucherId;

    //补剂的规格等详细信息
    private String supplementDetail;

    //数量
    private Integer number;

    //金额
    private BigDecimal amount;
}
