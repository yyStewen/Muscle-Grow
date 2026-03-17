package com.musclegrow.controller.user;

import com.musclegrow.constant.StatusConstant;
import com.musclegrow.entity.Supplement;
import com.musclegrow.result.Result;
import com.musclegrow.service.SupplementService;
import com.musclegrow.vo.SupplementVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-补剂浏览接口")
public class SupplementController {
    @Autowired
    private SupplementService supplementService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询补剂")
    public Result<List<SupplementVO>> list(Long categoryId) {

        //构造redis中的key，规则：dish_分类id
        String key = "supplement_" + categoryId;

        //查询redis中是否存在菜品数据
        List<SupplementVO> list = (List<SupplementVO>) redisTemplate.opsForValue().get(key);
        if(list != null && list.size() > 0){
            //如果存在，直接返回，无须查询数据库
            return Result.success(list);
        }

        Supplement supplement = new Supplement();
        supplement.setCategoryId(categoryId);
        supplement.setStatus(StatusConstant.ENABLE);//查询起售中的菜品

        //如果不存在，查询数据库，将查询到的数据放入redis中
        list = supplementService.listWithDetail(supplement);
        redisTemplate.opsForValue().set(key, list);

        return Result.success(list);
    }

}