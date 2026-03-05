package com.beyond.order_system.common.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CommonErrorDto {
    private int statusCode;
    private String errorMessage;
}
