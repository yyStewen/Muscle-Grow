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
public class UserVoucherVO implements Serializable {

    private Long id;

    private String title;

    private BigDecimal payValue;

    private BigDecimal actualValue;

    private Integer stock;

    private LocalDateTime beginTime;

    private LocalDateTime endTime;

    private Boolean purchased;
}
