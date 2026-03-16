package com.musclegrow.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀优惠券
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("voucher")
public class Voucher implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    //优惠券标题
    private String title;

    //用户购买优惠券需支付的价格（元）
    private BigDecimal payValue;

    //使用优惠券能抵扣的金额（元）
    private BigDecimal actualValue;

    //优惠券库存
    private Integer stock;

    //秒杀开始时间
    private LocalDateTime beginTime;

    //秒杀结束时间
    private LocalDateTime endTime;

    //优惠券状态：1投放中，2已结束，3优惠券下架
    private Integer status;

    //创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    //更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    //创建人
    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    //修改人
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;
}
