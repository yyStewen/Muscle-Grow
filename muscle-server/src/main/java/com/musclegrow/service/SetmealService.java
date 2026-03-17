package com.musclegrow.service;


import com.musclegrow.dto.SetmealDTO;
import com.musclegrow.dto.SetmealPageQueryDTO;
import com.musclegrow.entity.Setmeal;
import com.musclegrow.entity.Supplement;
import com.musclegrow.result.PageResult;
import com.musclegrow.vo.SetmealVO;
import com.musclegrow.vo.SupplementItemVO;

import java.util.List;

public interface SetmealService {
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    List<Supplement> listByCategoryId(Long categoryId);

    void saveWithDish(SetmealDTO setmealDTO);

    void deleteBatch(List<Long> ids);

    SetmealVO getByIdWithDish(Long id);

    void update(SetmealDTO setmealDTO);

    void startOrStop(Integer status, Long id);

    List<Setmeal> list(Setmeal setmeal);

    List<SupplementItemVO> getSupplementItemBySetmealId(Long id);
}
