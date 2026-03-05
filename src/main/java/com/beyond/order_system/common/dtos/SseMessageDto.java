package com.beyond.order_system.common.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SseMessageDto {
    private String receiver;
    private String sender;
    private String message;
}
