package com.musclegrow.controller.user;

import com.musclegrow.constant.StatusConstant;
import com.musclegrow.entity.Setmeal;
import com.musclegrow.result.Result;
import com.musclegrow.service.SetmealService;
import com.musclegrow.vo.SupplementItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
@Api(tags = "C端-套餐浏览接口")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    /**
     * 条件查询
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询套餐")
    @Cacheable(cacheNames = "setmealCache",key = "#categoryId") //key: setmealCache::100
    public Result<List<Setmeal>> list(Long categoryId) {
        Setmeal setmeal = new Setmeal();
        setmeal.setCategoryId(categoryId);
        setmeal.setStatus(StatusConstant.ENABLE);

        List<Setmeal> list = setmealService.list(setmeal);//动态条件查询套餐
        return Result.success(list);
    }

    /**
     * 根据套餐id查询包含的菜品列表
     *
     * @param id
     * @return
     */
    @GetMapping("/supplement/{id}")
    @ApiOperation("根据套餐id查询包含的补剂列表")
    public Result<List<SupplementItemVO>> dishList(@PathVariable("id") Long id) {
        List<SupplementItemVO> list = setmealService.getSupplementItemBySetmealId(id);
        return Result.success(list);
    }
}