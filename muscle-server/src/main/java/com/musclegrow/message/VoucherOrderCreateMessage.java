package com.musclegrow.message;

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
public class VoucherOrderCreateMessage implements Serializable {

    private String requestId;

    private Long userId;

    private Long voucherId;

    private String orderNumber;

    private BigDecimal payAmount;

    private LocalDateTime requestTime;
}
