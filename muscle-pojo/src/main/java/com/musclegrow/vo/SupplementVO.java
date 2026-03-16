package com.musclegrow.vo;

import com.musclegrow.entity.SupplementDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplementVO implements Serializable {

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
    //更新时间
    private LocalDateTime updateTime;
    //分类名称
    private String categoryName;
    //补剂关联的详细信息
    private List<SupplementDetail> details = new ArrayList<>();
}
