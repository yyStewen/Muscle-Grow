package com.musclegrow.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class ShoppingCartDTO implements Serializable {

    private Long supplementId;
    private Long setmealId;
    private String supplementDetail;

}
