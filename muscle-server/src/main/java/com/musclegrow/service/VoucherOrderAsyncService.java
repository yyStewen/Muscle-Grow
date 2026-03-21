package com.musclegrow.service;

import com.musclegrow.entity.Orders;
import com.musclegrow.message.VoucherOrderCreateMessage;

public interface VoucherOrderAsyncService {

    Orders createPendingVoucherOrder(VoucherOrderCreateMessage message);
}
