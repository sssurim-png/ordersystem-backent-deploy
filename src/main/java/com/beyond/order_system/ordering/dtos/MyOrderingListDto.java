package com.beyond.order_system.ordering.dtos;

import com.beyond.order_system.ordering.domain.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MyOrderingListDto {
    private Long id;
    private String memberEmail;
    private Status orderStatus;
    private List<OrderingDetailsListDto> orderDetails;
}
