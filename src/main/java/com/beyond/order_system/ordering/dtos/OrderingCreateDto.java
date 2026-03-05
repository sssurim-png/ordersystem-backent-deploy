package com.beyond.order_system.ordering.dtos;

import com.beyond.order_system.ordering.domain.OrderingDetails;
import com.beyond.order_system.product.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderingCreateDto {
    private Long productId;
    private int productCount;
}
