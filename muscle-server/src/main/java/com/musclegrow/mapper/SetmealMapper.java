package com.musclegrow.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.musclegrow.dto.SetmealPageQueryDTO;
import com.musclegrow.entity.Setmeal;
import com.musclegrow.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SetmealMapper extends BaseMapper<Setmeal> {
    Page<SetmealVO> pageQuery(@Param("page") Page<SetmealVO> page, @Param("setmealPageQueryDTO")SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 根据分类id查询套餐的数量
     * @param categoryId
     * @return
     */
    /**
    default Integer countByCategoryId(Long categoryId) {
        return Math.toIntExact(this.selectCount(
                new LambdaQueryWrapper<Setmeal>()
                        .eq(Setmeal::getCategoryId, categoryId)
        ));
    }
     */

}
