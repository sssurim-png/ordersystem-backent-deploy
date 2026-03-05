package com.beyond.order_system.ordering.domain;

import com.beyond.order_system.member.domain.Member;
import com.beyond.order_system.product.domain.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderingDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int quantity;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Ordering ordering;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    @Builder.Default
    private LocalDateTime created_time=LocalDateTime.now();

}
