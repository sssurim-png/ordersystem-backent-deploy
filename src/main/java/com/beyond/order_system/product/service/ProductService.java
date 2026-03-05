package com.beyond.order_system.product.service;

import com.beyond.order_system.common.auth.JwtTokenProvider;
import com.beyond.order_system.member.domain.Member;
import com.beyond.order_system.member.repository.MemberRepository;
import com.beyond.order_system.product.domain.Product;
import com.beyond.order_system.product.dtos.*;
import com.beyond.order_system.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Transactional
@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final S3Client s3Client;

    private final RedisTemplate<String, String> redisTemplate;
    @Value("${aws.s3.bucket1}")
    private String bucket;


    public ProductService(ProductRepository productRepository, S3Client s3Client, JwtTokenProvider jwtTokenProvider, MemberRepository memberRepository, @Qualifier("stockInventory") RedisTemplate<String, String> redisTemplate) {
        this.productRepository = productRepository;
        this.s3Client = s3Client;
        this.memberRepository = memberRepository;
        this.redisTemplate = redisTemplate;
    }

    public Long save(ProductCreateDto dto) {
        Optional<Product> opt_product = productRepository.findByName(dto.getName());
        if (opt_product.isPresent()) {
            throw new IllegalArgumentException("이미 있는 상품입니다.");
        } else {
            String email=SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
            Member member=memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("잘못된 접근입니다."));
            Product entityProduct = dto.toEntity(member);
            Product product = productRepository.save(entityProduct);
            if (dto.getProductImage() != null) {
                String fileName = "user-" + product.getName() + "-productImage-" + dto.getProductImage().getOriginalFilename();
                PutObjectRequest request = PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(fileName)
                        .contentType(dto.getProductImage().getContentType())
                        .build();
                try {
                    s3Client.putObject(request, RequestBody.fromBytes(dto.getProductImage().getBytes()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                String imgUrl = s3Client.utilities().getUrl(a -> a.bucket(bucket).key(fileName)).toExternalForm();
                product.updateProductImageUrl(imgUrl);
            }
//            동시성 문제 해결을 위해 redis에 재고세팅
            redisTemplate.opsForValue().set(String.valueOf(product.getId()), String.valueOf(product.getStockQuantity()));
            return product.getId();
        }
    }
    @Transactional(readOnly = true)
    public ProductDetailDto findById(Long id) {
        Product product=productRepository.findById(id).orElseThrow(()->new EntityNotFoundException("없는 상품입니다."));
        return ProductDetailDto.fromEntity(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductListDto> findAll(Pageable pageable, ProductSearchDto dto) {
        Specification<Product> specification = new Specification<Product>() {
            @Override
            public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicateList = new ArrayList<>();

                if (dto.getProductName() != null) {
                    predicateList.add(criteriaBuilder.like(root.get("name"), "%"+dto.getProductName()+"%"));
                }
                if (dto.getCategory() != null) {
                    predicateList.add(criteriaBuilder.equal(root.get("category"), dto.getCategory()));
                }

                Predicate[] predicateArr = new Predicate[predicateList.size()];
                for (int i = 0; i < predicateArr.length; i++) {
                    predicateArr[i] = predicateList.get(i);
                }
                Predicate predicate = criteriaBuilder.and(predicateArr);
                return predicate;
            }
        };

        Page<Product> productList = productRepository.findAll(specification, pageable);
//        Page객체 안에 Entity -> Dto로 쉽게 변환할수 있는 편의 제공
        return productList.map(p -> ProductListDto.fromEntity(p));
    }

    public void update(Long id, ProductUpdateDto dto) {
        Product product=productRepository.findById(id).orElseThrow(()->new EntityNotFoundException("없는 상품입니다."));
        product.updateProduct(dto);

//        수정할 이미지가 있으면
        if(dto.getProductImage()!=null) {
//            이미지를 수정 하는 경우 : 삭제 후 추가
//            DB에 기존 이미지 경로가 있는 경우 -> 삭제 후 새로 추가
            if(product.getImage_path()!=null) {
                String imgUrl=product.getImage_path();
                String fileName=imgUrl.substring(imgUrl.lastIndexOf("/")+1);
                s3Client.deleteObject(a->a.bucket(bucket).key(fileName));
            }
//            product.updateProduct(dto) 에서 이미지 외에 다른 dto 요소들은 이미 수정됨
            String newFileName = "user-" + product.getName() + "-productImage-" + dto.getProductImage().getOriginalFilename();
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(newFileName)
                    .contentType(dto.getProductImage().getContentType())
                    .build();
            try {
                s3Client.putObject(request, RequestBody.fromBytes(dto.getProductImage().getBytes()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String newImgUrl = s3Client.utilities().getUrl(a -> a.bucket(bucket).key(newFileName)).toExternalForm();
            product.updateProductImageUrl(newImgUrl);

//            수정할 이미지(받아온 dto안에 이미지)가 없으면
        }else {
//            이미지를 삭제하고자 하는 경우
//            기존 DB에 이미지 경로가 있는 경우에만 삭제
            if(product.getImage_path()!=null) {
                String imgUrl=product.getImage_path();
                String fileName=imgUrl.substring(imgUrl.lastIndexOf("/")+1);
                s3Client.deleteObject(a->a.bucket(bucket).key(fileName));
            }
        }
    }
}
