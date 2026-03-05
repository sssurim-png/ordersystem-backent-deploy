package com.beyond.order_system.member.domain;


import com.beyond.order_system.ordering.domain.Ordering;
import com.beyond.order_system.product.domain.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;
    @Builder.Default
    private LocalDateTime createdTime = LocalDateTime.now();
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<Product> productList;
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<Ordering> orderList;
}
