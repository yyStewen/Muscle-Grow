package com.musclegrow.service;


import com.musclegrow.vo.BusinessDataVO;
import com.musclegrow.vo.OrderOverViewVO;
import com.musclegrow.vo.SetmealOverViewVO;
import com.musclegrow.vo.SupplementOverViewVO;

import java.time.LocalDateTime;

public interface WorkspaceService {

    /**
     * 根据时间段统计营业数据
     * @param begin
     * @param end
     * @return
     */
    BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end);

    /**
     * 查询订单管理数据
     * @return
     */
    OrderOverViewVO getOrderOverView();

    /**
     * 查询菜品总览
     * @return
     */
    SupplementOverViewVO getSupplementOverView();

    /**
     * 查询套餐总览
     * @return
     */
    SetmealOverViewVO getSetmealOverView();

}