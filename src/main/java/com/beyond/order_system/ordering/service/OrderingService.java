package com.beyond.order_system.ordering.service;

import com.beyond.order_system.common.service.RabbitMqStockService;
import com.beyond.order_system.common.service.SseAlarmService;
import com.beyond.order_system.member.domain.Member;
import com.beyond.order_system.member.repository.MemberRepository;
import com.beyond.order_system.member.service.MemberService;
import com.beyond.order_system.ordering.domain.Ordering;
import com.beyond.order_system.ordering.domain.OrderingDetails;
import com.beyond.order_system.ordering.domain.Status;
import com.beyond.order_system.ordering.dtos.MyOrderingListDto;
import com.beyond.order_system.ordering.dtos.OrderingCreateDto;
import com.beyond.order_system.ordering.dtos.OrderingDetailsListDto;
import com.beyond.order_system.ordering.dtos.OrderingListDto;
import com.beyond.order_system.ordering.repository.OrderingDetailsRepository;
import com.beyond.order_system.ordering.repository.OrderingRepository;
import com.beyond.order_system.product.domain.Product;
import com.beyond.order_system.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderingService {
    private final OrderingDetailsRepository orderingDetailsRepository;
    private final OrderingRepository orderingRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final SseAlarmService sseAlarmService;
    private final RedisTemplate<String, String> redisTemplate;

    public OrderingService(OrderingDetailsRepository orderingDetailsRepository, OrderingRepository orderingRepository, MemberRepository memberRepository, ProductRepository productRepository, SseAlarmService sseAlarmService, @Qualifier("stockInventory") RedisTemplate<String, String> redisTemplate, RabbitMqStockService rabbitMqStockService) {
        this.orderingDetailsRepository = orderingDetailsRepository;
        this.orderingRepository = orderingRepository;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
        this.sseAlarmService = sseAlarmService;
        this.redisTemplate = redisTemplate;
    }
    public Long save(List<OrderingCreateDto> dtoList) {
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("없는 회원입니다."));

        Ordering ordering = Ordering.builder()
                .member(member)
                .build();

        orderingRepository.save(ordering);
        for (OrderingCreateDto dto : dtoList) {
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("상품이 없습니다."));

            if(product.getStockQuantity()<dto.getProductCount()) {
                throw new IllegalArgumentException("재고가 부족합니다. 현재 "+product.getName() +"의 주문 가능 수량은 "+product.getStockQuantity()+"개입니다.");
            }

            product.updateStockQuantity(dto.getProductCount());
            OrderingDetails detail = OrderingDetails.builder()
                    .ordering(ordering)
                    .product(product)
                    .quantity(dto.getProductCount())
                    .build();
            orderingDetailsRepository.save(detail);

        }
        return ordering.getId();
    }

    @Transactional(readOnly = true)
    public List<OrderingListDto> findAll() {
//       return orderingRepository.findAll().stream().map(o->OrderingListDto.builder()
//                .id(o.getId())
//                .memberEmail(o.getMember().getEmail())
//                .orderStatus(o.getOrderStatus())
//                .orderDetails(o.getOrderDetails().stream().map(od-> OrderingDetailsListDto.builder()
//                        .detailId(od.getId())
//                        .productName(od.getProduct().getName())
//                        .productCount(od.getQuantity())
//                        .build()).toList()).build()).toList();

        return orderingRepository.findAll().stream().map(o->OrderingListDto.fromEntity(o)).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderingListDto> findAllMine() {
        String email=SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
//        return orderingRepository.findAll().stream().filter(o->o.getMember().getEmail().equals(email)).map(o->MyOrderingListDto.builder()
//                .id(o.getId())
//                .memberEmail(email)
//                .orderDetails(o.getOrderDetails().stream().map(od->OrderingDetailsListDto.builder()
//                        .detailId(od.getId())
//                        .productName(od.getProduct().getName())
//                        .productCount(od.getQuantity())
//                        .build()).toList()).orderStatus(o.getOrderStatus()).build()).toList();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("없는 회원입니다."));
        return orderingRepository.findAllByMember(member).stream().map(o->OrderingListDto.fromEntity(o)).toList();
    }
}
