package com.beyond.order_system.product.controller;

import com.beyond.order_system.product.dtos.*;
import com.beyond.order_system.product.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Long productCreate(@ModelAttribute ProductCreateDto dto) {
        return productService.save(dto);
    }

    @GetMapping("/detail/{id}")
    public ProductDetailDto findById(@PathVariable Long id) {
        return productService.findById(id);
    }

    @GetMapping("/list")
    public Page<ProductListDto> findAll(@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,  ProductSearchDto searchDto) {
        return productService.findAll(pageable, searchDto);
    }

    @PutMapping("/update/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable Long id, @ModelAttribute ProductUpdateDto dto) {
        productService.update(id,dto);
    }
}
