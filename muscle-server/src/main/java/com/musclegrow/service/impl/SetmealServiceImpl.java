package com.musclegrow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.musclegrow.constant.MessageConstant;
import com.musclegrow.constant.StatusConstant;
import com.musclegrow.dto.SetmealDTO;
import com.musclegrow.dto.SetmealPageQueryDTO;
import com.musclegrow.entity.Setmeal;
import com.musclegrow.entity.SetmealSupplement;
import com.musclegrow.entity.Supplement;
import com.musclegrow.exception.DeletionNotAllowedException;
import com.musclegrow.exception.SetmealEnableFailedException;
import com.musclegrow.mapper.SetmealMapper;
import com.musclegrow.mapper.SetmealSupplementMapper;
import com.musclegrow.mapper.SupplementMapper;
import com.musclegrow.result.PageResult;
import com.musclegrow.service.SetmealService;
import com.musclegrow.vo.SetmealVO;
import com.musclegrow.vo.SupplementItemVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SupplementMapper supplementMapper;
    @Autowired
    private SetmealSupplementMapper setmealSupplementMapper;

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        //实现套餐的分页查询
        int pageNum = setmealPageQueryDTO.getPage();
        int pageSize = setmealPageQueryDTO.getPageSize();
        // 创建 MyBatis-Plus 的 Page 对象
        Page<SetmealVO> page = new Page<>(pageNum, pageSize);

        //执行查询
        Page<SetmealVO> pageResult = setmealMapper.pageQuery(page, setmealPageQueryDTO);

        //封装返回结果
        return new PageResult(pageResult.getTotal(), pageResult.getRecords());

    }

    @Override
    public List<Supplement> listByCategoryId(Long categoryId) {
        Supplement supplement = Supplement.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        // 根据分类查询处于启售状态的补剂
        return supplementMapper.list(supplement);
    }

    /**
     * 新增套餐，同时需要保存套餐和补剂的关联关系
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        //向套餐表插入数据
        setmealMapper.insert(setmeal);

        //获取生成的套餐id
        Long setmealId = setmeal.getId();

        List<SetmealSupplement> setmealSupplements = setmealDTO.getSetmealSupplements();
        for (SetmealSupplement setmealSupplement : setmealSupplements) {
            setmealSupplement.setSetmealId(setmealId);
        }

        //保存关联关系
        setmealSupplementMapper.insertBatch(setmealSupplements);



    }

    @Override
    public void deleteBatch(List<Long> ids) {
        ids.forEach(id -> {
            Setmeal setmeal = setmealMapper.selectById(id);
            if(StatusConstant.ENABLE == setmeal.getStatus()){
                //起售中的套餐不能删除
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        });

        ids.forEach(setmealId -> {
            //删除套餐表中的数据
            setmealMapper.deleteById(setmealId);
            //删除套餐菜品关系表中的数据
            setmealSupplementMapper.delete(new LambdaQueryWrapper<SetmealSupplement>().eq(SetmealSupplement::getSetmealId, setmealId));
        });

    }

    /**
     * 根据id查询套餐和套餐菜品关系
     *
     * @param id
     * @return
     */
    @Override
    public SetmealVO getByIdWithDish(Long id) {
        Setmeal setmeal = setmealMapper.selectById(id);
        List<SetmealSupplement> setmealSupplements = setmealSupplementMapper.
                selectList(new LambdaQueryWrapper<SetmealSupplement>().eq(SetmealSupplement::getSetmealId, id));


        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        setmealVO.setSetmealSupplements(setmealSupplements);

        return setmealVO;
    }

    /**
     * 修改套餐，同时需要更新套餐和补剂的关联关系
     * @param setmealDTO
     */
    @Override
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        //1、修改套餐表，执行update
        setmealMapper.updateById(setmeal);

        //套餐id
        Long setmealId = setmealDTO.getId();

        //删除套餐和补剂的关联关系，操作setmeal_supplement表，执行delete
        setmealSupplementMapper.delete(new LambdaQueryWrapper<SetmealSupplement>().eq(SetmealSupplement::getSetmealId, setmealId));

        List<SetmealSupplement> setmealSupplements = setmealDTO.getSetmealSupplements();
        for (SetmealSupplement setmealSupplement : setmealSupplements) {
            setmealSupplement.setSetmealId(setmealId);
        }

        //重新插入套餐和补剂的关联关系，操作setmeal_supplement表，执行insert
        //3、重新插入套餐和菜品的关联关系，操作setmeal_dish表，执行insert
        setmealSupplementMapper.insertBatch(setmealSupplements);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        //起售套餐时，判断套餐内是否有停售补剂，有停售补剂提示"套餐内包含未启售补剂，无法启售"
        if(status == StatusConstant.ENABLE){
            //select a.* from dish a left join setmeal_dish b on a.id = b.dish_id where b.setmeal_id = ?

            List<Supplement> supplements = supplementMapper.getBySetmealId(id);

            if(supplements != null && supplements.size() > 0){
                for (Supplement supplement : supplements) {
                    if(supplement.getStatus() == StatusConstant.DISABLE){
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                }
            }
        }

        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.updateById(setmeal);
    }

    @Override
    public List<Setmeal> list(Setmeal setmeal) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(setmeal.getName() != null, Setmeal::getName, setmeal.getName());
        queryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        List<Setmeal> list = setmealMapper.selectList(queryWrapper);
        return list;
    }

    /**
     * 根据套餐id查询包含的补剂
     * @param id
     * @return
     */
    @Override
    public List<SupplementItemVO> getSupplementItemBySetmealId(Long id) {
        return setmealSupplementMapper.getSupplementItemBySetmealId(id);
    }


}
