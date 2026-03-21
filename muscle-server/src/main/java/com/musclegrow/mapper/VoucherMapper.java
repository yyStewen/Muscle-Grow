package com.musclegrow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.musclegrow.entity.Voucher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface VoucherMapper extends BaseMapper<Voucher> {

    @Update("update voucher " +
            "set stock = stock - 1 " +
            "where id = #{id} " +
            "and stock > 0 " +
            "and status = 1 " +
            "and begin_time <= #{now} " +
            "and end_time >= #{now}")
    int deductStock(@Param("id") Long id, @Param("now") LocalDateTime now);

    @Update("update voucher set stock = stock + 1 where id = #{id}")
    int restoreStock(@Param("id") Long id);
}
