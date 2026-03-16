package com.musclegrow.service;


import com.musclegrow.dto.SetmealDTO;
import com.musclegrow.dto.SetmealPageQueryDTO;
import com.musclegrow.entity.Supplement;
import com.musclegrow.result.PageResult;
import com.musclegrow.vo.SetmealVO;

import java.util.List;

public interface SetmealService {
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    List<Supplement> list(Long categoryId);

    void saveWithDish(SetmealDTO setmealDTO);

    void deleteBatch(List<Long> ids);

    SetmealVO getByIdWithDish(Long id);

    void update(SetmealDTO setmealDTO);

    void startOrStop(Integer status, Long id);
}
