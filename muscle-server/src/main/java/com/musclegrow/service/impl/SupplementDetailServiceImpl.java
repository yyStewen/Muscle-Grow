package com.musclegrow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.musclegrow.entity.SupplementDetail;
import com.musclegrow.mapper.SupplementDetailMapper;
import com.musclegrow.service.SupplementDetailService;
import org.springframework.stereotype.Service;

import static net.bytebuddy.implementation.InvokeDynamic.lambda;

@Service
public class SupplementDetailServiceImpl extends ServiceImpl<SupplementDetailMapper, SupplementDetail> implements SupplementDetailService {
    @Override
    public void deleteBySupplementId(Long supplementId) {
        //根据补剂id删除对应的细节数据
        this.remove(new QueryWrapper<SupplementDetail>().eq("supplement_id", supplementId));


    }
}
