package com.musclegrow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.musclegrow.context.BaseContext;
import com.musclegrow.dto.ShoppingCartDTO;
import com.musclegrow.entity.Setmeal;
import com.musclegrow.entity.ShoppingCart;
import com.musclegrow.entity.Supplement;
import com.musclegrow.exception.BaseException;
import com.musclegrow.mapper.SetmealMapper;
import com.musclegrow.mapper.ShoppingCartMapper;
import com.musclegrow.mapper.SupplementMapper;
import com.musclegrow.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private SupplementMapper supplementMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        validateShoppingCartDTO(shoppingCartDTO);

        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(getCurrentUserId());
        shoppingCart.setSupplementDetail(normalizeSupplementDetail(shoppingCart.getSupplementDetail()));

        List<ShoppingCart> list = getMatchedShoppingCartList(shoppingCart);
        if (!list.isEmpty()) {
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.updateById(cart);
            return;
        }

        fillShoppingCartProductInfo(shoppingCartDTO, shoppingCart);
        shoppingCart.setNumber(1);
        shoppingCart.setCreateTime(LocalDateTime.now());
        shoppingCartMapper.insert(shoppingCart);
    }

    @Override
    public List<ShoppingCart> showShoppingCart() {
        Long userId = getCurrentUserId();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .build();
        return getShoppingCartList(shoppingCart);
    }

    @Override
    public void cleanShoppingCart() {
        Long userId = getCurrentUserId();
        shoppingCartMapper.delete(
                new LambdaQueryWrapper<ShoppingCart>().eq(ShoppingCart::getUserId, userId)
        );
    }

    @Override
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        validateShoppingCartDTO(shoppingCartDTO);

        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(getCurrentUserId());
        shoppingCart.setSupplementDetail(normalizeSupplementDetail(shoppingCart.getSupplementDetail()));

        List<ShoppingCart> list = getMatchedShoppingCartList(shoppingCart);
        if (list.isEmpty()) {
            return;
        }

        ShoppingCart currentCart = list.get(0);
        if (currentCart.getNumber() == 1) {
            shoppingCartMapper.deleteById(currentCart.getId());
            return;
        }

        currentCart.setNumber(currentCart.getNumber() - 1);
        shoppingCartMapper.updateById(currentCart);
    }

    private void fillShoppingCartProductInfo(ShoppingCartDTO shoppingCartDTO, ShoppingCart shoppingCart) {
        if (shoppingCartDTO.getSupplementId() != null) {
            Supplement supplement = supplementMapper.selectById(shoppingCartDTO.getSupplementId());
            if (supplement == null) {
                throw new BaseException("补剂不存在");
            }

            shoppingCart.setName(supplement.getName());
            shoppingCart.setImage(supplement.getImage());
            shoppingCart.setAmount(supplement.getPrice());
            return;
        }

        Setmeal setmeal = setmealMapper.selectById(shoppingCartDTO.getSetmealId());
        if (setmeal == null) {
            throw new BaseException("套餐不存在");
        }

        shoppingCart.setName(setmeal.getName());
        shoppingCart.setImage(setmeal.getImage());
        shoppingCart.setAmount(setmeal.getPrice());
    }

    private List<ShoppingCart> getShoppingCartList(ShoppingCart shoppingCart) {
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(shoppingCart.getUserId() != null, ShoppingCart::getUserId, shoppingCart.getUserId());
        queryWrapper.eq(shoppingCart.getSupplementId() != null, ShoppingCart::getSupplementId, shoppingCart.getSupplementId());
        queryWrapper.eq(shoppingCart.getSetmealId() != null, ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        queryWrapper.eq(
                shoppingCart.getSupplementDetail() != null,
                ShoppingCart::getSupplementDetail,
                shoppingCart.getSupplementDetail()
        );
        return shoppingCartMapper.selectList(queryWrapper);
    }

    private List<ShoppingCart> getMatchedShoppingCartList(ShoppingCart shoppingCart) {
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, shoppingCart.getUserId());
        queryWrapper.eq(
                shoppingCart.getSupplementId() != null,
                ShoppingCart::getSupplementId,
                shoppingCart.getSupplementId()
        );
        queryWrapper.eq(
                shoppingCart.getSetmealId() != null,
                ShoppingCart::getSetmealId,
                shoppingCart.getSetmealId()
        );

        if (shoppingCart.getSupplementId() != null) {
            if (shoppingCart.getSupplementDetail() == null) {
                queryWrapper.isNull(ShoppingCart::getSupplementDetail);
            } else {
                queryWrapper.eq(ShoppingCart::getSupplementDetail, shoppingCart.getSupplementDetail());
            }
        }

        if (shoppingCart.getSetmealId() != null) {
            queryWrapper.isNull(ShoppingCart::getSupplementDetail);
        }

        return shoppingCartMapper.selectList(queryWrapper);
    }

    private void validateShoppingCartDTO(ShoppingCartDTO shoppingCartDTO) {
        if (shoppingCartDTO == null) {
            throw new BaseException("购物车商品参数错误");
        }

        boolean hasSupplement = shoppingCartDTO.getSupplementId() != null;
        boolean hasSetmeal = shoppingCartDTO.getSetmealId() != null;
        if (hasSupplement == hasSetmeal) {
            throw new BaseException("购物车商品参数错误");
        }

        shoppingCartDTO.setSupplementDetail(normalizeSupplementDetail(shoppingCartDTO.getSupplementDetail()));
    }

    private String normalizeSupplementDetail(String supplementDetail) {
        return StringUtils.hasText(supplementDetail) ? supplementDetail.trim() : null;
    }

    private Long getCurrentUserId() {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException("用户未登录");
        }
        return userId;
    }
}
