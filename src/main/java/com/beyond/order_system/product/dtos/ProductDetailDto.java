package com.beyond.order_system.product.dtos;

import com.beyond.order_system.product.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductDetailDto {
    private Long id;
    private String name;
    private String category;
    private int price;
    private int stockQuantity;
    private String imagePath;

    public static ProductDetailDto fromEntity(Product product) {
        return ProductDetailDto.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .category(product.getCategory())
                .imagePath(product.getImage_path())
                .build();
    }
}
