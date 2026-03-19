package com.musclegrow.service;

import com.musclegrow.vo.OrderSubmitVO;
import com.musclegrow.vo.UserVoucherVO;

import java.util.List;

public interface UserVoucherService {

    List<UserVoucherVO> listAvailable();

    OrderSubmitVO purchase(Long voucherId);
}
