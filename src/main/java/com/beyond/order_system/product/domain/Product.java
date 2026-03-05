package com.beyond.order_system.product.domain;

import com.beyond.order_system.member.domain.Member;
import com.beyond.order_system.product.dtos.ProductUpdateDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Min(0)
    private int price;
    private String category;
    @Min(0)
    private int stockQuantity;
    private String image_path;
    @Builder.Default
    private LocalDateTime createdTime = LocalDateTime.now();
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public void updateProductImageUrl(String productImageUrl) {
        this.image_path=productImageUrl;
    }
    public void updateStockQuantity(int orderQuantity) {
        this.stockQuantity=this.stockQuantity-orderQuantity;
    }
    public void updateProduct(ProductUpdateDto dto) {
        this.name=dto.getName();
        this.category=dto.getCategory();
        this.stockQuantity=dto.getStockQuantity();
        this.price=dto.getPrice();
    }
}
