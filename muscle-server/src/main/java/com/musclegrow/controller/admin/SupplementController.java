package com.musclegrow.controller.admin;

import com.musclegrow.dto.SupplementDTO;
import com.musclegrow.dto.SupplementPageQueryDTO;
import com.musclegrow.result.PageResult;
import com.musclegrow.result.Result;
import com.musclegrow.service.SupplementService;
import com.musclegrow.vo.SupplementVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/admin/supplement")
@Api(tags = "补剂相关接口")
@Slf4j
public class SupplementController {

    @Autowired
    private SupplementService supplementService;

    /**
     * 新增补剂
     *
     * @param supplementDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增补剂")
    public Result save(@RequestBody SupplementDTO supplementDTO) {
        log.info("新增补剂：{}", supplementDTO);
        supplementService.saveWithSupplementDetail(supplementDTO);// 后绪步骤开发

        //清理缓存数据
        String key = "supplement_" + supplementDTO.getCategoryId();
        cleanCache(key);
        return Result.success();
    }

    /**
     * 补剂分页查询
     *
     * @param supplementPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("补剂分页查询")
    public Result<PageResult> page(SupplementPageQueryDTO supplementPageQueryDTO) {
        log.info("补剂分页查询:{}", supplementPageQueryDTO);
        PageResult pageResult = supplementService.pageQuery(supplementPageQueryDTO);// 后绪步骤定义
        return Result.success(pageResult);
    }

    /**
     * 菜品批量删除
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("菜品批量删除")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("菜品批量删除：{}", ids);
        supplementService.deleteBatch(ids);//后绪步骤实现

        cleanCache("supplement_*");//删除所有缓存数据
        return Result.success();
    }


    /**
     * 根据id查询菜品
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询补剂")
    public Result<SupplementVO> getById(@PathVariable Long id) {
        log.info("根据id查询补剂：{}", id);
        SupplementVO supplementVO = supplementService.getByIdWithSupplementDetail(id);//后绪步骤实现
        return Result.success(supplementVO);
    }

    /**
     * 修改补剂
     *
     * @param supplementDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改补剂")
    public Result update(@RequestBody SupplementDTO supplementDTO) {
        log.info("修改菜品：{}", supplementDTO);
        supplementService.updateWithDetail(supplementDTO);

        //清理缓存数据
        cleanCache("supplement_*");//删除所有缓存数据
        return Result.success();
    }

    /**
     * 补剂起售停售
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("补剂起售停售")
    public Result<String> startOrStop(@PathVariable Integer status, Long id){
        supplementService.startOrStop(status,id);

        //将所有的补剂缓存数据清理掉，所有以supplement_开头的key
        cleanCache("supplement_*");
        return Result.success();
    }

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 清理缓存数据
     * @param pattern
     */
    private void cleanCache(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
