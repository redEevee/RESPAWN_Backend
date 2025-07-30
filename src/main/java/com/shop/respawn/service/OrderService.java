package com.shop.respawn.service;

import com.shop.respawn.domain.*;
import com.shop.respawn.dto.*;
import com.shop.respawn.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

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
    private final RefundRequestRepository refundRequestRepository;

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

        // 관련 정보 설정
        setPaymentInfoFromOrderItems(order, order.getOrderItems());

        // 재고 차감
        reduceStockFromOrderItems(order.getOrderItems());

        // 주문 상태 주문 완료로 변경
        order.setStatus(OrderStatus.ORDERED);

        System.out.println("orderRequest = " + orderRequest.getCartItemIds());

        // 장바구니에서 주문된 아이템들 제거
        if (orderRequest.getCartItemIds() != null &&
                !orderRequest.getCartItemIds().isEmpty() &&
                orderRequest.getCartItemIds().stream().anyMatch(Objects::nonNull)) {
            Cart cart = cartRepository.findByBuyerId(buyerId)
                    .orElseThrow(() -> new RuntimeException("장바구니를 찾을 수 없습니다"));

            cart.getCartItems().removeIf(cartItem ->
                    order.getOrderItems().stream().anyMatch(orderItem ->
                            orderItem.getItemId().equals(cartItem.getItemId())
                    )
            );
            cartRepository.save(cart);
        }

        // 최종 저장
        Order savedOrder = orderRepository.save(order);

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

    // OrderItem 기반 정보 설정
    private void setPaymentInfoFromOrderItems(Order order, List<OrderItem> orderItems) {
        // 총 금액 계산
        int totalAmount = orderItems.stream()
                .mapToInt(orderItem -> orderItem.getOrderPrice() * orderItem.getCount())
                .sum();

        // 주문명 생성
        String orderName = generateOrderNameFromOrderItems(orderItems);

        // pgOrderId 생성
        String pgOrderId = "ORDER_" + order.getId() + "_" + System.currentTimeMillis();

        // 필드 설정
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

    /**
     * 주문 환불 신청 처리
     */
    @Transactional
    public void processRefundRequest(Long orderId, Long buyerId, RefundRequestDto refundRequestDto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
        if (!order.getBuyer().getId().equals(buyerId)) {
            throw new RuntimeException("해당 주문을 환불 신청할 권한이 없습니다.");
        }
        // 환불 신청 가능한지 상태 확인 (ORDERED, PAID 등)
        if (!(order.getStatus() == OrderStatus.ORDERED || order.getStatus() == OrderStatus.PAID)) {
            throw new RuntimeException("환불 신청이 불가능한 주문 상태입니다.");
        }

        // 환불 신청 엔티티 생성 및 저장
        RefundRequest refund = new RefundRequest();
        refund.setOrder(order);
        refund.setBuyerId(buyerId);
        refund.setReason(refundRequestDto.getReason());
        refund.setDetail(refundRequestDto.getDetail());
        refund.setRequestTime(LocalDateTime.now());

        refundRequestRepository.save(refund);

        // 상태값을 환불 신청으로 변경
        order.setStatus(OrderStatus.REFUND_REQUESTED);
        orderRepository.save(order);
    }

    /**
     * 판매자가 자신의 아이템의 환불 건 조회
     */
    public List<RefundRequestDto> getRefundRequestsBySeller(Long sellerId) {
        // 1. 모든 환불 요청 조회 (JPA 기준)
        List<RefundRequest> allRefunds = refundRequestRepository.findAll();

        // 2. 자신(판매자) 상품에 포함된 환불 요청만 필터링
        List<RefundRequestDto> result = new ArrayList<>();

        for (RefundRequest refund : allRefunds) {
            Order order = refund.getOrder(); // JPA
            boolean isMyOrder = order.getOrderItems().stream().allMatch(orderItem -> {
                // Item은 MongoDB → itemService.getItemById 사용
                System.out.println("sellerId = " + sellerId);
                Item item = itemService.getItemById(orderItem.getItemId());
                boolean equals = item.getSellerId().equals(String.valueOf(sellerId));
                System.out.println("sellerId = " + sellerId);
                return equals;
            });
            if (isMyOrder) {
                // 환불 DTO 변환
                result.add(RefundRequestDto.from(refund, order));
            }
        }
        return result;
    }

    /**
     * 판매자가 환불 승인/완료 처리 (구매자용 환불 신청과 별도)
     */
    @Transactional
    public void processRefundCompletionBySeller(Long orderId, Long sellerId) {
        // 1. 주문 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

        // 2. 주문의 모든 상품이 해당 판매자의 상품인지 확인
        boolean hasSellerItem = order.getOrderItems().stream().allMatch(orderItem -> {
            Item item = itemRepository.findById(orderItem.getItemId())
                    .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다: " + orderItem.getItemId()));
            // item.getSellerId()는 String 타입일 수 있으니 sellerId.toString() 등 타입 일치 고려
            return item.getSellerId().equals(String.valueOf(sellerId));
        });

        if (!hasSellerItem) {
            throw new RuntimeException("해당 주문의 상품은 이 판매자의 상품이 아닙니다.");
        }

        // 3. 결제 상태 확인
        if (!"SUCCESS".equals(order.getPaymentStatus())) {
            throw new RuntimeException("결제 완료된 주문만 환불 가능합니다");
        }

        // 4. 환불 신청 상태인지 체크
        if (order.getStatus() != OrderStatus.REFUND_REQUESTED) {
            throw new RuntimeException("환불 신청된 주문만 환불 완료 처리할 수 있습니다.");
        }

        // 5. 결제 환불 처리 (필요 시 연동)
        // ex) paymentService.cancelPaymentByOrderId(orderId, "고객 환불 승인");

        try {
            // 6. 재고 복원
            restoreStockFromOrder(order);

            // 7. 주문 상태를 환불로 변경
            order.setStatus(OrderStatus.REFUNDED);
            order.setPaymentStatus("REFUNDED");

            // 8 주문 저장
            orderRepository.save(order);

            log.info("환불 처리 완료 - orderId: {}", orderId);

        } catch (Exception e) {
            log.error("환불 처리 중 오류 발생 - orderId: {}, error: {}", orderId, e.getMessage());
            throw new RuntimeException("환불 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 주문에서 재고 복원
     */
    private void restoreStockFromOrder(Order order) {
        for (OrderItem orderItem : order.getOrderItems()) {
            Item item = itemRepository.findById(orderItem.getItemId())
                    .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다: " + orderItem.getItemId()));

            // 재고 복원
            item.addStock(orderItem.getCount());
            itemRepository.save(item);

            log.debug("재고 복원 완료 - itemId: {}, quantity: {}", item.getId(), orderItem.getCount());
        }
    }

    /**
     * 환불 가능한 주문 목록 조회
     */
    @Transactional(readOnly = true)
    public List<OrderHistoryDto> getRefundableOrders(Long buyerId) {
        // ORDERED 또는 PAID 상태이면서 결제 완료된 주문들을 조회
        List<Order> refundableOrders = orderRepository.findByBuyerIdAndStatusInAndPaymentStatus(
                buyerId,
                List.of(OrderStatus.ORDERED, OrderStatus.PAID),
                "SUCCESS"
        );

        List<OrderHistoryDto> orderHistoryDtos = new ArrayList<>();

        for (Order order : refundableOrders) {
            List<OrderHistoryItemDto> itemDtos = new ArrayList<>();

            for (OrderItem orderItem : order.getOrderItems()) {
                try {
                    Item item = itemService.getItemById(orderItem.getItemId());
                    OrderHistoryItemDto itemDto = OrderHistoryItemDto.from(orderItem, item);
                    itemDtos.add(itemDto);
                } catch (Exception e) {
                    log.error("환불 가능 주문 조회 중 아이템 정보 로드 실패 - itemId: {}", orderItem.getItemId());
                }
            }

            OrderHistoryDto orderDto = new OrderHistoryDto(order, itemDtos);
            orderHistoryDtos.add(orderDto);
        }

        return orderHistoryDtos;
    }

    /**
     * 환불 내역 조회
     */
    @Transactional(readOnly = true)
    public List<OrderHistoryDto> getRefundHistory(Long buyerId) {
        List<Order> refundedOrders = orderRepository.findByBuyer_IdAndStatusOrderByOrderDateDesc(
                buyerId, OrderStatus.REFUNDED);

        List<OrderHistoryDto> refundHistoryDtos = new ArrayList<>();

        for (Order order : refundedOrders) {
            List<OrderHistoryItemDto> itemDtos = new ArrayList<>();

            for (OrderItem orderItem : order.getOrderItems()) {
                try {
                    Item item = itemService.getItemById(orderItem.getItemId());
                    OrderHistoryItemDto itemDto = OrderHistoryItemDto.from(orderItem, item);
                    itemDtos.add(itemDto);
                } catch (Exception e) {
                    log.error("환불 내역 조회 중 아이템 정보 로드 실패 - itemId: {}", orderItem.getItemId());
                }
            }

            OrderHistoryDto orderDto = new OrderHistoryDto(order, itemDtos);
            refundHistoryDtos.add(orderDto);
        }

        return refundHistoryDtos;
    }

    /**
     * 주문 취소 처리 (배송 상태 확인 추가)
     */
    @Transactional
    public void processOrderCancel(Long orderId, Long buyerId) {
        // 1. 주문 조회 및 권한 확인
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다"));

        if (!order.getBuyer().getId().equals(buyerId)) {
            throw new RuntimeException("해당 주문을 취소할 권한이 없습니다");
        }

        // 2. 취소 가능 상태 확인 (주문 상태 + 배송 상태)
        if (!isCancellableStatus(order.getStatus())) {
            throw new RuntimeException("취소가 불가능한 주문 상태입니다: " + order.getStatus());
        }

        // 3. 배송 상태 확인 (핵심 추가 부분)
        if (!isCancellableDeliveryStatus(order)) {
            throw new RuntimeException("배송이 시작된 상품은 취소할 수 없습니다. 배송 완료 후 환불을 신청해주세요.");
        }

        try {
            // 4. 결제 상태에 따른 처리
            if ("SUCCESS".equals(order.getPaymentStatus())) {
                // 결제 완료된 경우 - 실제 결제 취소 처리 필요
                processPaymentCancel(order);
            }

            // 5. 재고가 이미 차감되었다면 복원
            if (needStockRestore(order.getStatus())) {
                restoreStockFromOrder(order);
            }

            // 6. 주문 상태를 취소로 변경
            order.setStatus(OrderStatus.CANCELED);
            order.setPaymentStatus("CANCELED");

            // 7. 주문 저장
            orderRepository.save(order);

            log.info("주문 취소 처리 완료 - orderId: {}, buyerId: {}", orderId, buyerId);

        } catch (Exception e) {
            log.error("주문 취소 처리 중 오류 발생 - orderId: {}, error: {}", orderId, e.getMessage());
            throw new RuntimeException("주문 취소 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 재고 복원이 필요한 상태인지 확인
     */
    private boolean needStockRestore(OrderStatus status) {
        // TEMPORARY 상태는 재고 차감이 안되었으므로 복원 불필요
        return status == OrderStatus.ORDERED || status == OrderStatus.PAID;
    }

    /**
     * 결제 취소 처리
     */
    private void processPaymentCancel(Order order) {

    }

    /**
     * 배송 상태 기준으로 취소 가능 여부 확인 (신규 추가)
     */
    private boolean isCancellableDeliveryStatus(Order order) {
        // 배송 정보가 없는 경우 (아직 배송 준비 전) - 취소 가능
        if (order.getDelivery() == null) {
            return true;
        }

        DeliveryStatus deliveryStatus = order.getDelivery().getStatus();

        // 배송 준비 상태에서만 취소 가능
        // 배송 중이나 배송 완료 상태에서는 취소 불가
        return deliveryStatus == DeliveryStatus.READY;
    }

    /**
     * 취소 가능한 상태인지 확인 (기존 메서드 유지)
     */
    private boolean isCancellableStatus(OrderStatus status) {
        // TEMPORARY: 임시 주문 (재고 차감 전)
        // ORDERED: 주문 완료 (재고 차감됨, 결제 대기 중)
        // PAID: 결제 완료 (배송 준비 단계)
        return status == OrderStatus.TEMPORARY ||
                status == OrderStatus.ORDERED ||
                status == OrderStatus.PAID;
    }

    /**
     * 취소 가능한 주문 목록 조회 (배송 상태 확인 추가)
     */
    @Transactional(readOnly = true)
    public List<OrderHistoryDto> getCancellableOrders(Long buyerId) {
        // TEMPORARY, ORDERED, PAID 상태의 주문들을 조회
        List<Order> potentialCancellableOrders = orderRepository.findByBuyerIdAndStatusIn(
                buyerId,
                List.of(OrderStatus.TEMPORARY, OrderStatus.ORDERED, OrderStatus.PAID)
        );

        // 배송 상태까지 확인하여 실제 취소 가능한 주문만 필터링
        List<Order> cancellableOrders = potentialCancellableOrders.stream()
                .filter(this::isCancellableDeliveryStatus)
                .toList();

        List<OrderHistoryDto> orderHistoryDtos = new ArrayList<>();

        for (Order order : cancellableOrders) {
            List<OrderHistoryItemDto> itemDtos = new ArrayList<>();

            for (OrderItem orderItem : order.getOrderItems()) {
                try {
                    Item item = itemService.getItemById(orderItem.getItemId());
                    OrderHistoryItemDto itemDto = OrderHistoryItemDto.from(orderItem, item);
                    itemDtos.add(itemDto);
                } catch (Exception e) {
                    log.error("취소 가능 주문 조회 중 아이템 정보 로드 실패 - itemId: {}", orderItem.getItemId());
                }
            }

            OrderHistoryDto orderDto = new OrderHistoryDto(order, itemDtos);
            orderHistoryDtos.add(orderDto);
        }

        return orderHistoryDtos;
    }

    /**
     * 취소 내역 조회
     */
    @Transactional(readOnly = true)
    public List<OrderHistoryDto> getCancelHistory(Long buyerId) {
        List<Order> cancelledOrders = orderRepository.findByBuyer_IdAndStatusOrderByOrderDateDesc(
                buyerId, OrderStatus.CANCELED);

        List<OrderHistoryDto> cancelHistoryDtos = new ArrayList<>();

        for (Order order : cancelledOrders) {
            List<OrderHistoryItemDto> itemDtos = new ArrayList<>();

            for (OrderItem orderItem : order.getOrderItems()) {
                try {
                    Item item = itemService.getItemById(orderItem.getItemId());
                    OrderHistoryItemDto itemDto = OrderHistoryItemDto.from(orderItem, item);
                    itemDtos.add(itemDto);
                } catch (Exception e) {
                    log.error("취소 내역 조회 중 아이템 정보 로드 실패 - itemId: {}", orderItem.getItemId());
                }
            }

            OrderHistoryDto orderDto = new OrderHistoryDto(order, itemDtos);
            cancelHistoryDtos.add(orderDto);
        }

        return cancelHistoryDtos;
    }

    /**
     * 카트아이템에서 오더아이템으로 변환
     */
    private OrderItem convertCartItemToOrderItem(CartItem cartItem) {
        OrderItem orderItem = new OrderItem();
        orderItem.setItemId(cartItem.getItemId());
        orderItem.setOrderPrice(cartItem.getCartPrice());  // 장바구니 가격을 주문 가격으로
        orderItem.setCount(cartItem.getCount());
        return orderItem;
    }
}
