package com.musclegrow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.musclegrow.constant.MessageConstant;
import com.musclegrow.constant.StatusConstant;
import com.musclegrow.dto.SupplementDTO;
import com.musclegrow.dto.SupplementPageQueryDTO;
import com.musclegrow.entity.Setmeal;
import com.musclegrow.entity.Supplement;
import com.musclegrow.entity.SupplementDetail;
import com.musclegrow.exception.DeletionNotAllowedException;
import com.musclegrow.mapper.SetmealMapper;
import com.musclegrow.mapper.SetmealSupplementMapper;
import com.musclegrow.mapper.SupplementMapper;
import com.musclegrow.result.PageResult;
import com.musclegrow.service.SupplementDetailService;
import com.musclegrow.service.SupplementService;
import com.musclegrow.vo.SupplementVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SupplementServicImpl extends ServiceImpl<SupplementMapper, Supplement> implements SupplementService {
    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SupplementMapper supplementMapper;

    @Autowired
    private SupplementDetailService supplementDetailService;

    @Autowired
    private SetmealSupplementMapper setmealSupplementMapper;

    /**
     * 新增补剂和对应的细节
     *
     * @param supplementDTO
     */
    @Transactional
    @Override
    public void saveWithSupplementDetail(SupplementDTO supplementDTO) {
        Supplement supplement = new Supplement();
        BeanUtils.copyProperties(supplementDTO, supplement);

        supplementMapper.insert(supplement);

        // 获取insert语句生成的主键值
        Long supplementId = supplement.getId();

        List<SupplementDetail> details = supplementDTO.getDetails();
        if (details != null && details.size() > 0) {
            details.forEach(detail -> {
                detail.setSupplementId(supplementId);
            });
            // 使用 IService 提供的 saveBatch 方法实现批量插入
            supplementDetailService.saveBatch(details);
        }

    }

    /**
     * 补剂分页查询
     * @param supplementPageQueryDTO
     * @return
     */

    @Override
    public PageResult pageQuery(SupplementPageQueryDTO supplementPageQueryDTO) {
        // 创建 MyBatis-Plus 的 Page 对象
        Page<SupplementVO> page = new Page<>(supplementPageQueryDTO.getPage(), supplementPageQueryDTO.getPageSize());

        // 执行查询
        Page<SupplementVO> pageResult = supplementMapper.pageQuery(page, supplementPageQueryDTO);

        // 封装返回结果
        return new PageResult(pageResult.getTotal(), pageResult.getRecords());
    }

    /**
     * 补剂批量删除
     *
     * @param ids
     */
    @Transactional // 事务
    public void deleteBatch(List<Long> ids) {
        // 判断当前补剂是否能够删除---是否存在起售中的补剂？？
        for (Long id : ids) {
            Supplement supplement = supplementMapper.selectById(id);
            if (supplement.getStatus() == StatusConstant.ENABLE) {
                // 当前补剂处于起售中，不能删除
                throw new DeletionNotAllowedException(MessageConstant.SUPPLEMENT_ON_SALE);
            }
        }

        // 判断当前补剂是否能够删除---是否被套餐关联了？？
        List<Long> setmealIds = setmealSupplementMapper.getSetmealIdsByDishIds(ids);// 对应：select setmeal_id from setmeal_supplement
                                                                         // where supplement_id in (1,2,3,4)
        if (setmealIds != null && setmealIds.size() > 0) {
            // 当前补剂被套餐关联了，不能删除
            throw new DeletionNotAllowedException(MessageConstant.SUPPLEMENT_BE_RELATED_BY_SETMEAL);
        }

        // 删除补剂表中的补剂数据
        for (Long id : ids) {
            supplementMapper.deleteById(id);// 后绪步骤实现
            // 删除补剂关联的细节数据
            supplementDetailService.deleteBySupplementId(id);// 后绪步骤实现
        }
    }

    /**
     * 补剂查询回显
     * @param id
     * @return
     */
    @Override
    public SupplementVO getByIdWithSupplementDetail(Long id) {
        //根据id查询补剂数据
        Supplement supplement = supplementMapper.selectById(id);

        //根据菜品id查询口味数据

        List<SupplementDetail> supplementDetails =
                supplementDetailService.list(new LambdaQueryWrapper<SupplementDetail>().eq(SupplementDetail::getSupplementId, id));

        //将查询到的数据封装到vo
        SupplementVO supplementVO = new SupplementVO();
        BeanUtils.copyProperties(supplement, supplementVO);
        supplementVO.setDetails(supplementDetails);

        return supplementVO;
    }

    /**
     * 修改补剂信息，同时需要更新对应的细节和规格数据
     *
     * @param supplementDTO
     */
    @Override
    public void updateWithDetail(SupplementDTO supplementDTO) {
        Supplement supplement = new Supplement();
        BeanUtils.copyProperties(supplementDTO, supplement);

        //修改基本信息
        supplementMapper.updateById(supplement);

        //删除原有细节数据
        supplementDetailService.deleteBySupplementId(supplementDTO.getId());

        //重新插入细节数据
        List<SupplementDetail> details = supplementDTO.getDetails();
        if (!CollectionUtils.isEmpty(details)) {
            details.forEach(detail -> {
                detail.setSupplementId(supplementDTO.getId());
            });
            supplementDetailService.saveBatch(details);
        }

    }
    /**
     * 补剂起售停售
     *
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Supplement supplement = Supplement.builder()
                .id(id)
                .status(status)
                .build();
        supplementMapper.updateById(supplement);

        // 如果是停售操作，还需要将包含当前补剂的套餐也停售
        if (status == StatusConstant.DISABLE) {
            //通过补剂id查询套餐id
            List<Long> dishIds = new ArrayList<>();
            dishIds.add(id);

            // select setmeal_id from setmeal_supplement where supplement_id in (?,?,?)
            List<Long> setmealIds = setmealSupplementMapper.getSetmealIdsByDishIds(dishIds);
            // 如果是停售操作，还需要将包含当前补剂的套餐也停售
            if (setmealIds != null && setmealIds.size() > 0) {
                for (Long setmealId : setmealIds) {
                    Setmeal setmeal = Setmeal.builder()
                            .id(setmealId)
                            .status(StatusConstant.DISABLE)
                            .build();
                    setmealMapper.updateById(setmeal);
                }
            }


        }



    }


    /**
     * 条件查询补剂和细节，规格信息
     * @param supplement
     * @return
     */
    @Override
    public List<SupplementVO> listWithDetail(Supplement supplement) {
        List<Supplement>supplementList=supplementMapper.list(supplement);

        List<SupplementVO>supplementVOList=new ArrayList<>();

        for(Supplement s:supplementList){
            SupplementVO supplementVO=new SupplementVO();
            BeanUtils.copyProperties(s,supplementVO);

            //根据补剂id查询对应细节
            List<SupplementDetail>details=supplementDetailService.list(
                    new LambdaQueryWrapper<SupplementDetail>().eq(SupplementDetail::getSupplementId, s.getId()));

            supplementVO.setDetails(details);
            supplementVOList.add(supplementVO);

        }
        return supplementVOList;
    }
}
