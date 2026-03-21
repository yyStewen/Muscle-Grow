package com.musclegrow.service;

public interface VoucherSeckillRedisService {

    Long tryReserve(Long voucherId, Long userId);

    void preloadStock(Long voucherId, Integer stock);

    void refreshStock(Long voucherId, Integer stock);

    void removeStock(Long voucherId);

    void rollbackReservation(Long voucherId, Long userId);

    void confirmPurchase(Long voucherId, Long userId);

    boolean hasBought(Long voucherId, Long userId);
}
