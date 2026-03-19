package com.musclegrow.service;

import com.musclegrow.vo.UserVoucherVO;

import java.util.List;

public interface UserVoucherService {

    List<UserVoucherVO> listAvailable();

    Long purchase(Long voucherId);
}
