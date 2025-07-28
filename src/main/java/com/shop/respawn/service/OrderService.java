package com.shop.respawn.service;

import com.shop.respawn.domain.*;
import com.shop.respawn.dto.OrderHistoryDto;
import com.shop.respawn.dto.OrderHistoryItemDto;
import com.shop.respawn.dto.OrderItemDetailDto;
import com.shop.respawn.dto.OrderRequestDto;
import com.shop.respawn.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final BuyerRepository buyerRepository;
    private final ItemRepository itemRepository;
    private final AddressRepository addressRepository;
    private final ItemService itemService;

    /**
     * 임시 주문 상세 조회
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getOrderDetails(Long orderId, Long buyerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다"));

        // 주문 소유권 확인
        if (!order.getBuyer().getId().equals(buyerId)) {
            throw new RuntimeException("해당 주문을 조회할 권한이 없습니다");
        }

        // OrderItemDetailDto 리스트로 변환
        List<OrderItemDetailDto> orderItemDetails = order.getOrderItems().stream()
                .map(orderItem -> {
                    Item item = itemRepository.findById(orderItem.getItemId())
                            .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다: " + orderItem.getItemId()));
                    return OrderItemDetailDto.from(orderItem, item);
                })
                .toList();

        // 총 금액 계산
        int totalAmount = orderItemDetails.stream()
                .mapToInt(OrderItemDetailDto::getTotalPrice)
                .sum();

        // 응답 데이터 구성
        Map<String, Object> response = new HashMap<>();
        response.put("orderId", order.getId());
        response.put("orderItems", orderItemDetails); // DTO 객체 그대로 반환
        response.put("totalAmount", totalAmount);
        response.put("itemCount", orderItemDetails.size());

        return response;
    }

    /**
     * 장바구니 선택 상품으로 주문페이지 이동 (선택된 CartItem을 OrderItem으로 복사만)
     */
    public Long prepareOrderSelectedFromCart(Long buyerId, OrderRequestDto orderRequest) {
        Buyer buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("구매자를 찾을 수 없습니다"));

        Cart cart = cartRepository.findByBuyerId(buyerId)
                .orElseThrow(() -> new RuntimeException("장바구니가 비어있습니다"));

        // 선택된 장바구니 아이템들 조회
        List<CartItem> selectedCartItems = cart.getCartItems().stream()
                .filter(cartItem -> orderRequest.getCartItemIds().contains(cartItem.getId()))
                .toList();

        if (selectedCartItems.isEmpty()) {
            throw new RuntimeException("선택된 상품이 없습니다");
        }

        // 선택된 CartItem을 OrderItem으로 변환만
        List<OrderItem> orderItems = selectedCartItems.stream()
                .map(this::convertCartItemToOrderItem)
                .toList();

        OrderItem[] orderItemArray = orderItems.toArray(new OrderItem[0]);

        // 임시 Order 생성 (배송 정보 없이)
        Order order = new Order();
        order.setBuyer(buyer);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.TEMPORARY);

        for (OrderItem orderItem : orderItemArray) {
            order.addOrderItem(orderItem);
        }

        // 임시 주문 저장
        Order savedOrder = orderRepository.save(order);
        return savedOrder.getId();
    }

    @Transactional
    public Long createTemporaryOrder(Long buyerId, String itemId, Integer count) {
        Buyer buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("구매자를 찾을 수 없습니다"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다"));

        if (count <= 0) {
            throw new RuntimeException("수량은 1 이상이어야 합니다.");
        }

        if (item.getStockQuantity() < count) {
            throw new RuntimeException("재고가 부족합니다.");
        }

        // 임시 주문 생성
        Order order = new Order();
        order.setBuyer(buyer);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.TEMPORARY); // 또는 임시 상태가 있으면 사용

        // 주문 아이템 생성
        OrderItem orderItem = new OrderItem();
        orderItem.setItemId(item.getId());
        orderItem.setCount(count);
        orderItem.setOrderPrice(item.getPrice());
        order.addOrderItem(orderItem);

        Order savedOrder = orderRepository.save(order);

        return savedOrder.getId();
    }

    /**
     * 선택된 상품 주문 완료 처리
     */
    public void completeSelectedOrder(Long orderId, OrderRequestDto orderRequest) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다"));

        Long buyerId = order.getBuyer().getId();

        // 재고 확인
        validateStockFromOrderItems(order.getOrderItems());

        // 배송 정보 설정
        Delivery delivery = createDeliveryWithAddressId(buyerId, orderRequest.getAddressId());
        order.setDelivery(delivery);

        // 토스페이먼츠 관련 정보 설정
        setTossPaymentInfoFromOrderItems(order, order.getOrderItems());

        // 재고 차감
        reduceStockFromOrderItems(order.getOrderItems());

        // 최종 저장
        Order savedOrder = orderRepository.save(order);

        // 장바구니에서 주문된 아이템들 제거
        if (orderRequest.getCartItemIds() != null && !orderRequest.getCartItemIds().isEmpty()) {
            Cart cart = cartRepository.findByBuyerId(buyerId)
                    .orElseThrow(() -> new RuntimeException("장바구니를 찾을 수 없습니다"));

            cart.getCartItems().removeIf(cartItem ->
                    order.getOrderItems().stream().anyMatch(orderItem ->
                            orderItem.getItemId().equals(cartItem.getItemId())
                    )
            );
            cartRepository.save(cart);
        }

    }

    // OrderItem 기반 재고 확인
    private void validateStockFromOrderItems(List<OrderItem> orderItems) {
        for (OrderItem orderItem : orderItems) {
            Item item = itemRepository.findById(orderItem.getItemId())
                    .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다: " + orderItem.getItemId()));
            if (item.getStockQuantity() < orderItem.getCount()) {
                throw new RuntimeException("재고가 부족합니다. 상품: " + item.getName() +
                        ", 요청수량: " + orderItem.getCount() +
                        ", 현재재고: " + item.getStockQuantity());
            }
        }
    }

    // OrderItem 기반 재고 차감
    private void reduceStockFromOrderItems(List<OrderItem> orderItems) {
        for (OrderItem orderItem : orderItems) {
            Item item = itemRepository.findById(orderItem.getItemId())
                    .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다: " + orderItem.getItemId()));
            item.removeStock(orderItem.getCount());
            itemRepository.save(item);
        }
    }

    // OrderItem 기반 토스페이먼츠 정보 설정
    private void setTossPaymentInfoFromOrderItems(Order order, List<OrderItem> orderItems) {
        // 총 금액 계산
        int totalAmount = orderItems.stream()
                .mapToInt(orderItem -> orderItem.getOrderPrice() * orderItem.getCount())
                .sum();

        // 주문명 생성
        String orderName = generateOrderNameFromOrderItems(orderItems);

        // tossOrderId 생성
        String pgOrderId = "ORDER_" + order.getId() + "_" + System.currentTimeMillis();

        // 토스페이먼츠 필드 설정
        order.setPgOrderId(pgOrderId);
        order.setOrderName(orderName);
        order.setTotalAmount(totalAmount);
        order.setPaymentStatus("READY");
    }

    // OrderItem 기반 주문명 생성
    private String generateOrderNameFromOrderItems(List<OrderItem> orderItems) {
        if (orderItems.isEmpty()) {
            return "상품";
        }

        // 첫 번째 상품 정보 조회
        OrderItem firstItem = orderItems.getFirst();
        Item item = itemRepository.findById(firstItem.getItemId())
                .orElse(null);
        String firstItemName = (item != null) ? item.getName() : "상품";

        int itemCount = orderItems.size();
        if (itemCount == 1) {
            return firstItemName;
        } else {
            return firstItemName + " 외 " + (itemCount - 1) + "건";
        }
    }

    /**
     * 주소 ID로 Delivery 생성
     */
    private Delivery createDeliveryWithAddressId(Long buyerId, Long addressId) {
        // 주소 조회 및 권한 확인
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("주소를 찾을 수 없습니다: " + addressId));

        // 주소 소유권 확인 (해당 구매자의 주소인지 검증)
        if (address.getBuyer() != null && !address.getBuyer().getId().equals(buyerId)) {
            throw new RuntimeException("해당 주소를 사용할 권한이 없습니다");
        }

        Delivery delivery = new Delivery();
        delivery.setAddress(address);
        delivery.setStatus(DeliveryStatus.READY);
        return delivery;
    }

    /**
     * 주문 ID로 구매자 ID 조회
     */
    @Transactional(readOnly = true)
    public Long getBuyerIdByOrderId(Long orderId) {
        log.debug("주문 ID로 구매자 ID 조회 시작 - orderId: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("주문을 찾을 수 없습니다 - orderId: {}", orderId);
                    return new RuntimeException("주문을 찾을 수 없습니다: " + orderId);
                });

        if (order.getBuyer() == null) {
            log.error("주문에 구매자 정보가 없습니다 - orderId: {}", orderId);
            throw new RuntimeException("주문에 구매자 정보가 없습니다: " + orderId);
        }

        Long buyerId = order.getBuyer().getId();
        log.debug("구매자 ID 조회 완료 - orderId: {}, buyerId: {}", orderId, buyerId);

        return buyerId;
    }

    /**
     * 구매자의 주문 내역을 최신순으로 조회하여 DTO 리스트로 반환
     */
    public List<OrderHistoryDto> getOrderHistory(Long buyerId) {
        // 1. 주문 목록 조회
        List<Order> orders = orderRepository.findByBuyer_IdOrderByOrderDateDesc(buyerId);

        // 2. 반환할 DTO 리스트 준비
        List<OrderHistoryDto> orderHistoryDtos = new ArrayList<>();

        for (Order order : orders) {
            // 2-1. 해당 주문의 아이템 목록 조회 및 DTO 변환
            List<OrderHistoryItemDto> itemDtos = new ArrayList<>();

            for (OrderItem orderItem : order.getOrderItems()) {
                try {
                    // MongoDB에서 Item 정보 조회
                    Item item = itemService.getItemById(orderItem.getItemId());

                    // DTO 변환 후 리스트에 추가
                    OrderHistoryItemDto itemDto = OrderHistoryItemDto.from(orderItem, item);
                    itemDtos.add(itemDto);
                } catch (Exception e) {
                    e.printStackTrace(); // 혹은 로그 찍기
                    // 필요하면 기본값 세팅 or 예외 무시
                }
            }

            // 2-2. 주문 단위 DTO 생성 후 최종 리스트에 추가
            OrderHistoryDto orderDto = new OrderHistoryDto(order, itemDtos);
            orderHistoryDtos.add(orderDto);
        }

        return orderHistoryDtos;
    }

    public OrderHistoryDto getLatestOrderByBuyerId(Long buyerId) {
        Order order = orderRepository.findTop1ByBuyer_IdAndStatusOrderByOrderDateDesc(buyerId, OrderStatus.ORDERED);

        if (order == null) {  // 주문 없으면 null임
            return null;       // null 반환해서 컨트롤러에서 204 No Content 처리 가능
        }

        // 주문 있으면 DTO 변환 진행
        List<OrderHistoryItemDto> itemDtos = order.getOrderItems().stream()
                .map(orderItem -> {
                    Item item = itemService.getItemById(orderItem.getItemId());
                    return OrderHistoryItemDto.from(orderItem, item);
                }).toList();

        return new OrderHistoryDto(order, itemDtos);
    }

    /**
     * 현재 사용자의 모든 임시 주문 삭제 (TEMPORARY 상태인 주문들을 일괄 삭제)
     */
    @Transactional
    public int deleteAllTemporaryOrders(Long buyerId) {
        // 해당 구매자의 TEMPORARY 상태인 모든 주문 조회
        List<Order> temporaryOrders = orderRepository.findByBuyerIdAndStatus(buyerId, OrderStatus.TEMPORARY);

        if (temporaryOrders.isEmpty()) {
            log.info("삭제할 임시 주문이 없습니다. buyerId: {}", buyerId);
            return 0;
        }

        // 모든 임시 주문 삭제
        orderRepository.deleteAll(temporaryOrders);

        int deletedCount = temporaryOrders.size();
        log.info("임시 주문 {}건이 삭제되었습니다. buyerId: {}", deletedCount, buyerId);

        return deletedCount;
    }

    private OrderItem convertCartItemToOrderItem(CartItem cartItem) {
        OrderItem orderItem = new OrderItem();
        orderItem.setItemId(cartItem.getItemId());
        orderItem.setOrderPrice(cartItem.getCartPrice());  // 장바구니 가격을 주문 가격으로
        orderItem.setCount(cartItem.getCount());
        return orderItem;
    }
}
