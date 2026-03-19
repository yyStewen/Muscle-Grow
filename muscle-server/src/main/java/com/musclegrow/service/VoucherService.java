package com.musclegrow.service;

import com.musclegrow.dto.VoucherDTO;
import com.musclegrow.dto.VoucherPageQueryDTO;
import com.musclegrow.result.PageResult;
import com.musclegrow.vo.VoucherVO;

public interface VoucherService {

    PageResult pageQuery(VoucherPageQueryDTO voucherPageQueryDTO);

    void save(VoucherDTO voucherDTO);

    VoucherVO getById(Long id);

    void update(VoucherDTO voucherDTO);

    void startOrStop(Integer status, Long id);
}
