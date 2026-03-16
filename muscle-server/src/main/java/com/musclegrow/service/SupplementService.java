package com.musclegrow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.musclegrow.dto.SupplementDTO;
import com.musclegrow.dto.SupplementPageQueryDTO;
import com.musclegrow.entity.Supplement;
import com.musclegrow.result.PageResult;
import com.musclegrow.vo.SupplementVO;

import java.util.List;

public interface SupplementService extends IService<Supplement> {
    /**
     * 新增补剂和补剂对应的规格和细节
     */
    void saveWithSupplementDetail(SupplementDTO supplementDTO);

    PageResult pageQuery(SupplementPageQueryDTO supplementPageQueryDTO);

    void deleteBatch(List<Long> ids);

    SupplementVO getByIdWithSupplementDetail(Long id);

    void updateWithDetail(SupplementDTO supplementDTO);

    void startOrStop(Integer status, Long id);
}
