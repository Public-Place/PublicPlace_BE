package com.springboot.publicplace.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultDto {
    private boolean success;
    private int code;
    private String msg;
}