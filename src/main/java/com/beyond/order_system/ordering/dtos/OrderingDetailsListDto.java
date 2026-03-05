package com.beyond.order_system.ordering.dtos;

import com.beyond.order_system.ordering.domain.OrderingDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderingDetailsListDto {
    private Long detailId;
    private String productName;
    private int productCount;

    public static OrderingDetailsListDto fromEntity(OrderingDetails orderingDetails) {
        return OrderingDetailsListDto.builder()
                .detailId(orderingDetails.getId())
                .productName(orderingDetails.getProduct().getName())
                .productCount(orderingDetails.getQuantity())
                .build();
    }
}
