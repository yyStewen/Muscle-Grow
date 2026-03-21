package com.musclegrow.service;

import com.musclegrow.vo.VoucherSeckillResultVO;

public interface VoucherSeckillResultService {

    void saveProcessing(String requestId);

    void saveSuccess(String requestId, Long orderId, String orderNumber);

    void saveFailed(String requestId, String message);

    VoucherSeckillResultVO getResult(String requestId);
}
