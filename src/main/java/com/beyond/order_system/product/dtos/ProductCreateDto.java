package com.beyond.order_system.product.dtos;

import com.beyond.order_system.member.domain.Member;
import com.beyond.order_system.product.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductCreateDto {
    private String name;
    private int price;
    private String category;
    private int stockQuantity;
    private String image_path;
    private MultipartFile productImage;

    public Product toEntity(Member member) {
        return Product.builder()
                .name(this.name)
                .price(this.price)
                .category(this.category)
                .stockQuantity(this.stockQuantity)
                .member(member)
                .build();
    }
}
