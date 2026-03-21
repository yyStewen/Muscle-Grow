package com.musclegrow.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherSeckillResultVO implements Serializable {

    private String requestId;

    private String status;

    private Long orderId;

    private String orderNumber;

    private String message;
}
