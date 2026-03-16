package com.musclegrow.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplementItemVO implements Serializable {

    //补剂名称
    private String name;

    //份数
    private Integer copies;

    //补剂图片
    private String image;

    //补剂描述
    private String description;
}
