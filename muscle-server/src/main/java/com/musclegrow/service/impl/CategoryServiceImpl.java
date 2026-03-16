package com.musclegrow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.musclegrow.constant.MessageConstant;
import com.musclegrow.constant.StatusConstant;
import com.musclegrow.dto.CategoryDTO;
import com.musclegrow.dto.CategoryPageQueryDTO;
import com.musclegrow.entity.Category;
import com.musclegrow.entity.Supplement;
import com.musclegrow.exception.DeletionNotAllowedException;
import com.musclegrow.mapper.CategoryMapper;
import com.musclegrow.mapper.SetmealMapper;
import com.musclegrow.mapper.SupplementMapper;
import com.musclegrow.result.PageResult;
import com.musclegrow.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 分类业务层
 */
@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private SupplementMapper supplementMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 新增分类
     * 
     * @param categoryDTO
     */
    public void save(CategoryDTO categoryDTO) {
        Category category = new Category();
        // 属性拷贝
        BeanUtils.copyProperties(categoryDTO, category);

        // 分类状态默认为禁用状态0
        category.setStatus(StatusConstant.DISABLE);

        // MyMetaObjectHandler 自动填充创建时间、修改时间、创建人、修改人

        categoryMapper.insert(category);
    }

    /**
     * 分页查询
     * 
     * @param categoryPageQueryDTO
     * @return
     */
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        Page<Category> pageInfo = new Page<>(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());

        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        // name 模糊查询
        if (categoryPageQueryDTO.getName() != null && !categoryPageQueryDTO.getName().trim().isEmpty()) {
            queryWrapper.like(Category::getName, categoryPageQueryDTO.getName());
        }
        // type 等值查询
        if (categoryPageQueryDTO.getType() != null) {
            queryWrapper.eq(Category::getType, categoryPageQueryDTO.getType());
        }

        // order by sort asc , create_time desc
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getCreateTime);

        categoryMapper.selectPage(pageInfo, queryWrapper);

        return new PageResult(pageInfo.getTotal(), pageInfo.getRecords());
    }

    /**
     * 根据id删除分类
     * 
     * @param id
     */
    public void deleteById(Long id) {
        // 查询当前分类是否关联了补剂，如果关联了就抛出业务异常
        LambdaQueryWrapper<Supplement> supplementWrapper = new LambdaQueryWrapper<>();
        supplementWrapper.eq(Supplement::getCategoryId, id);
        Long count = supplementMapper.selectCount(supplementWrapper);
        if (count > 0) {
            // 当前分类下有补剂，不能删除
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SUPPLEMENT);
        }

        // 查询当前分类是否关联了套餐，如果关联了就抛出业务异常
        Integer setmealCount = setmealMapper.countByCategoryId(id);
        if (setmealCount > 0) {
            // 当前分类下有套餐，不能删除
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }

        // 删除分类数据
        categoryMapper.deleteById(id);
    }

    /**
     * 修改分类
     * 
     * @param categoryDTO
     */
    public void update(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);

        // mybatis-plus 将在 MyMetaObjectHandler 中填充 updateTime、updateUser

        categoryMapper.updateById(category);
    }

    /**
     * 启用、禁用分类
     * 
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id) {
        Category category = Category.builder()
                .id(id)
                .status(status)
                .build();
        categoryMapper.updateById(category);
    }

    /**
     * 根据类型查询分类
     * 
     * @param type
     * @return
     */
    public List<Category> list(Integer type) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getStatus, 1);
        if (type != null) {
            queryWrapper.eq(Category::getType, type);
        }
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getCreateTime);
        return categoryMapper.selectList(queryWrapper);
    }
}
