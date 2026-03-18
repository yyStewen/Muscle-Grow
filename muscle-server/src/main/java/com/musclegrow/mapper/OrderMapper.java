package com.musclegrow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.musclegrow.dto.GoodsSalesDTO;
import com.musclegrow.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper extends BaseMapper<Orders> {
    Double sumByMap(Map map);

    List<GoodsSalesDTO> getSalesTop10(LocalDateTime beginTime, LocalDateTime endTime);

    Integer countByMap(Map map);
}
