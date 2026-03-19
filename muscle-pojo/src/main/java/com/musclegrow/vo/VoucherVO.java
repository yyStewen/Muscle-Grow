package com.musclegrow.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherVO implements Serializable {

    private Long id;

    private String title;

    private BigDecimal payValue;

    private BigDecimal actualValue;

    private Integer stock;

    private LocalDateTime beginTime;

    private LocalDateTime endTime;

    /**
     * 数据库存储状态
     */
    private Integer status;

    /**
     * 页面展示状态
     */
    private Integer displayStatus;

    private String displayStatusLabel;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
