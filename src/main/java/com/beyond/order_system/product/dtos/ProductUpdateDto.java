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
public class ProductUpdateDto {
    private String name;
    private int price;
    private String category;
    private int stockQuantity;
//    이미지 수정은 일반적으로 별도의 api로 처리
    private MultipartFile productImage;

}
