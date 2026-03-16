package com.musclegrow.vo;

import com.musclegrow.entity.OrderDetail;
import com.musclegrow.entity.Orders;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderVO extends Orders implements Serializable {

    //订单补剂信息
    private String orderSupplements;

    //订单详情
    private List<OrderDetail> orderDetailList;

}
