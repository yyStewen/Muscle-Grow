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
public class VoucherSeckillSubmitVO implements Serializable {

    private String requestId;

    private String status;

    private String message;
}
