package com.beyond.order_system.ordering.dtos;

import com.beyond.order_system.ordering.domain.Ordering;
import com.beyond.order_system.ordering.domain.OrderingDetails;
import com.beyond.order_system.ordering.domain.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderingListDto {
    private Long id;
    private String memberEmail;
    private Status orderStatus;
    private List<OrderingDetailsListDto> orderDetails;

    public static OrderingListDto fromEntity(Ordering ordering) {
        List<OrderingDetailsListDto> orderingDetailsListDtoList = new ArrayList<>();
        for (OrderingDetails orderingDetails : ordering.getOrderDetails()) {
            orderingDetailsListDtoList.add(OrderingDetailsListDto.fromEntity(orderingDetails));
        }
        OrderingListDto orderingListDto = OrderingListDto.builder()
                .id(ordering.getId())
                .memberEmail(ordering.getMember().getEmail())
                .orderStatus(ordering.getOrderStatus())
                .orderDetails(orderingDetailsListDtoList)
                .build();
        return orderingListDto;
    }
}
