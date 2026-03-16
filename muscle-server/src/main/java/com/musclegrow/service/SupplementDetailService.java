package com.musclegrow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.musclegrow.entity.SupplementDetail;

public interface SupplementDetailService extends IService<SupplementDetail> {

    void deleteBySupplementId(Long id);
}
