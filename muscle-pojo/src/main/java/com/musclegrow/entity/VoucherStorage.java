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
import java.time.LocalDateTime;

/**
 * 用户优惠券仓库
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("voucher_storage")
public class VoucherStorage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    //所属用户id
    private Long userId;

    //关联的优惠券id
    private Long voucherId;

    //使用该券的补剂订单id
    private Long orderId;

    //优惠券名称（冗余存储）
    private String name;

    //优惠券抵扣金额
    private BigDecimal actualValue;

    //状态：1-未使用，2-已使用，3-已过期，4-锁定中
    private Integer status;

    //领取/购买时间
    private LocalDateTime createTime;

    //使用时间
    private LocalDateTime useTime;

    //过期时间
    private LocalDateTime expireTime;
}
