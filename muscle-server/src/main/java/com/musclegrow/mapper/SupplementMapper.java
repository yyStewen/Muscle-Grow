package com.musclegrow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.musclegrow.dto.SupplementPageQueryDTO;
import com.musclegrow.entity.Supplement;
import com.musclegrow.vo.SupplementVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface SupplementMapper extends BaseMapper<Supplement> {

    /**
     * 补剂分页查询
     * @param page 分页对象
     * @param dto 查询条件
     * @return 分页结果
     */
    Page<SupplementVO> pageQuery(Page<SupplementVO> page, @Param("dto") SupplementPageQueryDTO dto);

    List<Supplement> list(Supplement supplement);

    List<Supplement> getBySetmealId(Long id);

    Integer countByMap(Map map);
}
