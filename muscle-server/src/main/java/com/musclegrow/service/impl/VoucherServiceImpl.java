package com.musclegrow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.musclegrow.constant.VoucherStatusConstant;
import com.musclegrow.dto.VoucherDTO;
import com.musclegrow.dto.VoucherPageQueryDTO;
import com.musclegrow.entity.Voucher;
import com.musclegrow.exception.BaseException;
import com.musclegrow.mapper.VoucherMapper;
import com.musclegrow.result.PageResult;
import com.musclegrow.service.VoucherService;
import com.musclegrow.vo.VoucherVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class VoucherServiceImpl implements VoucherService {

    private static final String MSG_EDIT_ENDED = "\u5df2\u7ed3\u675f\u7684\u4f18\u60e0\u5238\u4e0d\u5141\u8bb8\u4fee\u6539";
    private static final String MSG_STATUS_INVALID = "\u4f18\u60e0\u5238\u72b6\u6001\u4e0d\u5408\u6cd5";
    private static final String MSG_OPERATE_ENDED = "\u5df2\u7ed3\u675f\u7684\u4f18\u60e0\u5238\u4e0d\u80fd\u518d\u64cd\u4f5c";
    private static final String MSG_TITLE_REQUIRED = "\u4f18\u60e0\u5238\u6807\u9898\u4e0d\u80fd\u4e3a\u7a7a";
    private static final String MSG_PAY_VALUE_INVALID = "\u652f\u4ed8\u91d1\u989d\u4e0d\u80fd\u5c0f\u4e8e0";
    private static final String MSG_ACTUAL_VALUE_INVALID = "\u62b5\u6263\u91d1\u989d\u5fc5\u987b\u5927\u4e8e0";
    private static final String MSG_AMOUNT_COMPARE_INVALID = "\u62b5\u6263\u91d1\u989d\u5fc5\u987b\u5927\u4e8e\u652f\u4ed8\u91d1\u989d";
    private static final String MSG_STOCK_INVALID = "\u5e93\u5b58\u5fc5\u987b\u5927\u4e8e0";
    private static final String MSG_TIME_REQUIRED = "\u5f00\u59cb\u65f6\u95f4\u548c\u7ed3\u675f\u65f6\u95f4\u4e0d\u80fd\u4e3a\u7a7a";
    private static final String MSG_TIME_INVALID = "\u7ed3\u675f\u65f6\u95f4\u5fc5\u987b\u665a\u4e8e\u5f00\u59cb\u65f6\u95f4";
    private static final String MSG_TITLE_DUPLICATED = "\u4f18\u60e0\u5238\u6807\u9898\u5df2\u5b58\u5728";
    private static final String MSG_STATUS_FILTER_INVALID = "\u4f18\u60e0\u5238\u72b6\u6001\u7b5b\u9009\u6761\u4ef6\u4e0d\u5408\u6cd5";
    private static final String MSG_VOUCHER_NOT_FOUND = "\u4f18\u60e0\u5238\u4e0d\u5b58\u5728";

    private static final String LABEL_ON_SALE = "\u6295\u653e\u4e2d";
    private static final String LABEL_ENDED = "\u5df2\u7ed3\u675f";
    private static final String LABEL_OFF_SHELF = "\u5df2\u4e0b\u67b6";
    private static final String LABEL_NOT_STARTED = "\u5f85\u5f00\u59cb";
    private static final String LABEL_UNKNOWN = "\u672a\u77e5\u72b6\u6001";

    @Autowired
    private VoucherMapper voucherMapper;

    @Override
    public PageResult pageQuery(VoucherPageQueryDTO voucherPageQueryDTO) {
        // 列表页需要展示实时状态，所以查询前先把已过期的券同步为“已结束”。
        syncExpiredStatus();

        Page<Voucher> page = new Page<>(voucherPageQueryDTO.getPage(), voucherPageQueryDTO.getPageSize());
        LambdaQueryWrapper<Voucher> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.like(StringUtils.hasText(voucherPageQueryDTO.getTitle()),
                Voucher::getTitle, voucherPageQueryDTO.getTitle());
        queryWrapper.ge(voucherPageQueryDTO.getBeginTime() != null,
                Voucher::getEndTime, voucherPageQueryDTO.getBeginTime());
        queryWrapper.le(voucherPageQueryDTO.getEndTime() != null,
                Voucher::getBeginTime, voucherPageQueryDTO.getEndTime());
        applyStatusFilter(queryWrapper, voucherPageQueryDTO.getStatus());
        queryWrapper.orderByDesc(Voucher::getCreateTime);

        voucherMapper.selectPage(page, queryWrapper);
        List<VoucherVO> records = page.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return new PageResult(page.getTotal(), records);
    }

    @Override
    public void save(VoucherDTO voucherDTO) {
        validateVoucher(voucherDTO, null);

        Voucher voucher = new Voucher();
        BeanUtils.copyProperties(voucherDTO, voucher);
        // 数据库存储只保留可持久化状态，“待开始”依赖 beginTime 动态计算。
        voucher.setStatus(resolvePersistStatus(voucherDTO.getStatus(), voucherDTO.getEndTime()));
        voucherMapper.insert(voucher);
    }

    @Override
    public VoucherVO getById(Long id) {
        // 详情页回显同样依赖最新状态，避免前端拿到已经过期但未落库的数据。
        syncExpiredStatus();
        return toVO(getVoucherOrThrow(id));
    }

    @Override
    public void update(VoucherDTO voucherDTO) {
        // 修改前先同步过期状态，避免已结束的券被继续编辑。
        syncExpiredStatus();

        Voucher currentVoucher = getVoucherOrThrow(voucherDTO.getId());
        if (Objects.equals(currentVoucher.getStatus(), VoucherStatusConstant.ENDED)) {
            throw new BaseException(MSG_EDIT_ENDED);
        }

        validateVoucher(voucherDTO, voucherDTO.getId());

        Voucher voucher = new Voucher();
        BeanUtils.copyProperties(voucherDTO, voucher);
        // 编辑时和新增保持同一套状态落库规则，避免“待开始”被当作独立持久化状态。
        voucher.setStatus(resolvePersistStatus(voucherDTO.getStatus(), voucherDTO.getEndTime()));
        voucherMapper.updateById(voucher);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        // 上下架动作依赖当前真实状态，先同步过期数据再做业务判断。
        syncExpiredStatus();

        if (!Objects.equals(status, VoucherStatusConstant.ON_SALE)
                && !Objects.equals(status, VoucherStatusConstant.OFF_SHELF)) {
            throw new BaseException(MSG_STATUS_INVALID);
        }

        Voucher voucher = getVoucherOrThrow(id);
        if (Objects.equals(voucher.getStatus(), VoucherStatusConstant.ENDED)) {
            throw new BaseException(MSG_OPERATE_ENDED);
        }

        Voucher updateVoucher = Voucher.builder()
                .id(id)
                .status(status)
                .build();
        voucherMapper.updateById(updateVoucher);
    }

    private void validateVoucher(VoucherDTO voucherDTO, Long currentId) {
        if (!StringUtils.hasText(voucherDTO.getTitle())) {
            throw new BaseException(MSG_TITLE_REQUIRED);
        }

        if (voucherDTO.getPayValue() == null || voucherDTO.getPayValue().compareTo(BigDecimal.ZERO) < 0) {
            throw new BaseException(MSG_PAY_VALUE_INVALID);
        }

        if (voucherDTO.getActualValue() == null || voucherDTO.getActualValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BaseException(MSG_ACTUAL_VALUE_INVALID);
        }

        if (voucherDTO.getActualValue().compareTo(voucherDTO.getPayValue()) <= 0) {
            throw new BaseException(MSG_AMOUNT_COMPARE_INVALID);
        }

        if (voucherDTO.getStock() == null || voucherDTO.getStock() <= 0) {
            throw new BaseException(MSG_STOCK_INVALID);
        }

        if (voucherDTO.getBeginTime() == null || voucherDTO.getEndTime() == null) {
            throw new BaseException(MSG_TIME_REQUIRED);
        }

        if (!voucherDTO.getEndTime().isAfter(voucherDTO.getBeginTime())) {
            throw new BaseException(MSG_TIME_INVALID);
        }

        if (voucherDTO.getStatus() != null
                && !Objects.equals(voucherDTO.getStatus(), VoucherStatusConstant.ON_SALE)
                && !Objects.equals(voucherDTO.getStatus(), VoucherStatusConstant.OFF_SHELF)) {
            throw new BaseException(MSG_STATUS_INVALID);
        }

        LambdaQueryWrapper<Voucher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Voucher::getTitle, voucherDTO.getTitle());
        queryWrapper.ne(currentId != null, Voucher::getId, currentId);

        Long count = voucherMapper.selectCount(queryWrapper);
        if (count != null && count > 0) {
            throw new BaseException(MSG_TITLE_DUPLICATED);
        }
    }

    private void applyStatusFilter(LambdaQueryWrapper<Voucher> queryWrapper, Integer status) {
        if (status == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        if (Objects.equals(status, VoucherStatusConstant.ON_SALE)) {
            queryWrapper.eq(Voucher::getStatus, VoucherStatusConstant.ON_SALE)
                    .le(Voucher::getBeginTime, now)
                    .ge(Voucher::getEndTime, now);
            return;
        }

        if (Objects.equals(status, VoucherStatusConstant.ENDED)) {
            queryWrapper.eq(Voucher::getStatus, VoucherStatusConstant.ENDED);
            return;
        }

        if (Objects.equals(status, VoucherStatusConstant.OFF_SHELF)) {
            queryWrapper.eq(Voucher::getStatus, VoucherStatusConstant.OFF_SHELF);
            return;
        }

        if (Objects.equals(status, VoucherStatusConstant.NOT_STARTED)) {
            // “待开始”不是数据库中的独立状态，而是投放中且开始时间未到的展示态。
            queryWrapper.eq(Voucher::getStatus, VoucherStatusConstant.ON_SALE)
                    .gt(Voucher::getBeginTime, now);
            return;
        }

        throw new BaseException(MSG_STATUS_FILTER_INVALID);
    }

    private VoucherVO toVO(Voucher voucher) {
        VoucherVO voucherVO = new VoucherVO();
        BeanUtils.copyProperties(voucher, voucherVO);

        // 前端列表和表单都使用展示状态，避免直接暴露持久化状态造成歧义。
        Integer displayStatus = resolveDisplayStatus(voucher);
        voucherVO.setDisplayStatus(displayStatus);
        voucherVO.setDisplayStatusLabel(resolveDisplayStatusLabel(displayStatus));
        return voucherVO;
    }

    private Integer resolveDisplayStatus(Voucher voucher) {
        if (Objects.equals(voucher.getStatus(), VoucherStatusConstant.ENDED)) {
            return VoucherStatusConstant.ENDED;
        }

        if (Objects.equals(voucher.getStatus(), VoucherStatusConstant.OFF_SHELF)) {
            return VoucherStatusConstant.OFF_SHELF;
        }

        LocalDateTime now = LocalDateTime.now();
        if (voucher.getBeginTime() != null && voucher.getBeginTime().isAfter(now)) {
            return VoucherStatusConstant.NOT_STARTED;
        }

        return VoucherStatusConstant.ON_SALE;
    }

    private String resolveDisplayStatusLabel(Integer displayStatus) {
        if (Objects.equals(displayStatus, VoucherStatusConstant.ON_SALE)) {
            return LABEL_ON_SALE;
        }
        if (Objects.equals(displayStatus, VoucherStatusConstant.ENDED)) {
            return LABEL_ENDED;
        }
        if (Objects.equals(displayStatus, VoucherStatusConstant.OFF_SHELF)) {
            return LABEL_OFF_SHELF;
        }
        if (Objects.equals(displayStatus, VoucherStatusConstant.NOT_STARTED)) {
            return LABEL_NOT_STARTED;
        }
        return LABEL_UNKNOWN;
    }

    private Integer resolvePersistStatus(Integer status, LocalDateTime endTime) {
        if (endTime != null && !endTime.isAfter(LocalDateTime.now())) {
            return VoucherStatusConstant.ENDED;
        }

        if (status == null) {
            return VoucherStatusConstant.ON_SALE;
        }

        return status;
    }

    private Voucher getVoucherOrThrow(Long id) {
        Voucher voucher = voucherMapper.selectById(id);
        if (voucher == null) {
            throw new BaseException(MSG_VOUCHER_NOT_FOUND);
        }
        return voucher;
    }

    private void syncExpiredStatus() {
        Voucher voucher = Voucher.builder()
                .status(VoucherStatusConstant.ENDED)
                .build();

        LambdaUpdateWrapper<Voucher> updateWrapper = new LambdaUpdateWrapper<>();
        // 这里走批量更新，把所有已到结束时间但仍未落库的券统一标记为“已结束”。
        updateWrapper.ne(Voucher::getStatus, VoucherStatusConstant.ENDED)
                .lt(Voucher::getEndTime, LocalDateTime.now());

        voucherMapper.update(voucher, updateWrapper);
    }
}
