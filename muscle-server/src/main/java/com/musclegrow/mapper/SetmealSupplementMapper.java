package com.musclegrow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.musclegrow.entity.SetmealSupplement;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealSupplementMapper extends BaseMapper<SetmealSupplement> {
    /**
     * 根据菜品id查询套餐id
     * @param ids
     * @return
     */
    List<Long> getSetmealIdsByDishIds(List<Long> ids);
}
