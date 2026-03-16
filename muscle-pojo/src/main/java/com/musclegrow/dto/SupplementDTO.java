package com.musclegrow.dto;

import com.musclegrow.entity.SupplementDetail;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class SupplementDTO implements Serializable {

    private Long id;
    //补剂名称
    private String name;
    //补剂分类id
    private Long categoryId;
    //补剂价格
    private BigDecimal price;
    //图片
    private String image;
    //描述信息
    private String description;
    //0 停售 1 起售
    private Integer status;
    //补剂详细信息
    private List<SupplementDetail> details = new ArrayList<>();

}
