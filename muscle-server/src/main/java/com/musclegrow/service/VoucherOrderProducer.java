package com.musclegrow.service;

import com.musclegrow.message.VoucherOrderCreateMessage;

public interface VoucherOrderProducer {

    void sendCreateOrderMessage(VoucherOrderCreateMessage message);
}
