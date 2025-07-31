package com.shop.respawn.service;

import com.shop.respawn.domain.*;
import com.shop.respawn.dto.*;
import com.shop.respawn.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.shop.respawn.dto.RefundRequestDetailDto.*;

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
    private final OrderItemRepository orderItemRepository;

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
        response.put("name", order.getBuyer().getName());
        response.put("phoneNumber", order.getBuyer().getPhoneNumber());
        response.put("email", order.getBuyer().getEmail());
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

        // 배송지 주소 조회 및 권한 체크
        Address address = addressRepository.findById(orderRequest.getAddressId())
                .orElseThrow(() -> new RuntimeException("주소를 찾을 수 없습니다"));

        for (OrderItem orderItem : order.getOrderItems()) {
            Delivery delivery = new Delivery();
            delivery.setAddress(address);
            delivery.setStatus(DeliveryStatus.READY);
            delivery.setOrderItem(orderItem);

            orderItem.setDelivery(delivery);
        }

        // 배송 정보 설정
//        Delivery delivery = createDeliveryWithAddressId(buyerId, orderRequest.getAddressId());
//        order.setDelivery(delivery);

        // 관련 정보 설정
        setPaymentInfoFromOrderItems(order, order.getOrderItems());

        // 재고 차감
        reduceStockFromOrderItems(order.getOrderItems());

        // 주문 상태 주문 완료로 변경
        order.setStatus(OrderStatus.PAID);

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
        orderRepository.save(order);

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
        Order order = orderRepository.findTop1ByBuyer_IdAndStatusOrderByOrderDateDesc(buyerId, OrderStatus.PAID);

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
     * 로그인한 구매자의 주문 내역 단건 조회
     */
    public OrderHistoryDto getOrderDetail(Long orderId, Long buyerId) {
        Order order = orderRepository.findByIdAndBuyerIdWithItems(orderId, buyerId)
                .orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다."));

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
     * 환불 가능한 목록 조회
     */
    @Transactional(readOnly = true)
    public List<OrderHistoryDto> getRefundableOrderItems(Long buyerId) {
        // 결제 성공(SUCCESS), 배송 준비/배송 중 등 환불 가능한 주문만 조회(상황에 따라 필터링 확장 가능)
        List<Order> orders = orderRepository.findByBuyerIdAndStatusInAndPaymentStatus(
                buyerId,
                List.of(OrderStatus.ORDERED, OrderStatus.PAID),
                "SUCCESS"
        );

        List<OrderHistoryDto> result = new ArrayList<>();

        for (Order order : orders) {
            // 환불 요청 또는 완료가 아닌 아이템만 필터링
            List<OrderItem> refundableItems = order.getOrderItems().stream()
                    .filter(oi -> oi.getRefundStatus() == RefundStatus.NONE)
                    .toList();

            if (!refundableItems.isEmpty()) {
                List<OrderHistoryItemDto> itemDtos = new ArrayList<>();
                for (OrderItem oi : refundableItems) {
                    try {
                        Item item = itemService.getItemById(oi.getItemId());
                        itemDtos.add(OrderHistoryItemDto.from(oi, item));
                    } catch (Exception e) {
                        log.error("환불 가능 내역 중 아이템 조회 오류 : itemId={}", oi.getItemId(), e);
                    }
                }
                result.add(new OrderHistoryDto(order, itemDtos));
            }
        }
        return result;
    }

    /**
     * 아이템 단위 환불 요청
     */
    @Transactional
    public void requestRefund(Long orderId, Long orderItemId, Long buyerId, String reason, String detail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

        if (!order.getBuyer().getId().equals(buyerId)) {
            throw new RuntimeException("해당 주문에 대한 권한이 없습니다.");
        }

        Buyer findBuyer = buyerRepository.findById(buyerId).get();

        Optional<OrderItem> optionalOrderItem = order.getOrderItems()
                .stream()
                .filter(oi -> oi.getId().equals(orderItemId))
                .findFirst();

        if (optionalOrderItem.isEmpty()) {
            throw new RuntimeException("해당 주문 아이템을 찾을 수 없습니다.");
        }

        OrderItem orderItem = optionalOrderItem.get();

        if (orderItem.getRefundStatus() != RefundStatus.NONE) {
            throw new RuntimeException("이미 환불 요청 또는 완료된 아이템입니다.");
        }

        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setOrderItem(orderItem);
        refundRequest.setBuyer(findBuyer);
        refundRequest.setRefundReason(reason);
        refundRequest.setRefundDetail(detail);
        refundRequest.setRequestedAt(LocalDateTime.now());
        orderItem.setRefundRequest(refundRequest);

        // 환불 요청 상태로 변경
        orderItem.setRefundStatus(RefundStatus.REQUESTED);

        orderRepository.save(order);
    }

    /**
     * 요청한 환불 목록 보기
     */
    @Transactional(readOnly = true)
    public List<OrderHistoryDto> getRefundRequestedItems(Long buyerId) {
        // 해당 buyer의 모든 주문을 가져온다
        List<Order> orders = orderRepository.findByBuyer_IdOrderByOrderDateDesc(buyerId);

        List<OrderHistoryDto> refundRequestedOrders = new ArrayList<>();

        for (Order order : orders) {
            // 주문 아이템 중 환불 요청 또는 완료 상태인 아이템 필터링
            List<OrderItem> refundItems = order.getOrderItems().stream()
                    .filter(oi -> oi.getRefundStatus() == RefundStatus.REQUESTED || oi.getRefundStatus() == RefundStatus.REFUNDED)
                    .toList();

            if (!refundItems.isEmpty()) {
                // 각 아이템에 대해 Item 정보 조회 및 DTO 변환
                List<OrderHistoryItemDto> itemDtos = new ArrayList<>();
                for (OrderItem oi : refundItems) {
                    try {
                        Item item = itemService.getItemById(oi.getItemId());
                        OrderHistoryItemDto dto = OrderHistoryItemDto.from(oi, item);
                        itemDtos.add(dto);
                    } catch (Exception e) {
                        // 예외 로깅 및 무시 또는 기본값 처리
                        log.error("환불 내역 아이템 조회 중 오류: itemId={}", oi.getItemId(), e);
                    }
                }
                refundRequestedOrders.add(new OrderHistoryDto(order, itemDtos));
            }
        }
        return refundRequestedOrders;
    }

    /**
     * 판매자 환불 요청 확인
     */
    @Transactional
    public List<RefundRequestDetailDto> getRefundRequestsForSeller(Long sellerId) {
        // 1. 판매자가 등록한 상품 id 목록 조회
        List<Item> sellerItems = itemService.getItemsBySellerId(String.valueOf(sellerId));
        Set<String> sellerItemIds = sellerItems.stream()
                .map(Item::getId)
                .collect(Collectors.toSet());

        // 2. 모든 주문 조회 (실제 운영 환경에선 조건절로 최적화 권장)
        List<Order> allOrders = orderRepository.findAll();

        // 3. 결과 담을 리스트 초기화
        List<RefundRequestDetailDto> result = new ArrayList<>();

        // 4. 각 주문별로 주문자(buyer), 주문 아이템 순회
        for (Order order : allOrders) {
            Buyer buyer = order.getBuyer();

            for (OrderItem oi : order.getOrderItems()) {
                // 주문아이템에 매핑된 배송 정보와 주소 획득
                Delivery delivery = oi.getDelivery();
                Address address = (delivery != null) ? delivery.getAddress() : null;

                // 5. 환불 요청 상태이고, 판매자 상품 목록에 포함된 아이템만 필터링
                if (oi.getRefundStatus() == RefundStatus.REQUESTED && sellerItemIds.contains(oi.getItemId())) {
                    // 6. 아이템 정보 조회
                    Item item = itemService.getItemById(oi.getItemId());
                    // 7. refundRequest 정보 가져오기
                    RefundRequest refundRequest = oi.getRefundRequest();

                    // 8. 내부 DTO 객체 생성
                    BuyerInfo buyerInfo = new BuyerInfo(buyer);
                    AddressInfo addressInfo = (address != null) ? new AddressInfo(address) : null;
                    RefundInfo refundInfo = new RefundInfo(refundRequest);

                    // 9. DTO 변환 후 결과에 추가
                    result.add(new RefundRequestDetailDto(order, oi, item, buyerInfo, addressInfo, refundInfo));
                }
            }
        }
        return result;
    }

    /**
     * 판매자 환불 요청 완료
     */
    @Transactional
    public void completeRefund(Long orderItemId, Long sellerId) {
        // 주문 아이템 조회
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new RuntimeException("주문 아이템을 찾을 수 없습니다: " + orderItemId));

        // 판매자 권한 검증: 해당 주문 아이템의 상품이 sellerId가 맞는지 확인
        Item item = itemService.getItemById(orderItem.getItemId());
        if (!item.getSellerId().equals(String.valueOf(sellerId))) {
            throw new RuntimeException("해당 판매자가 주문 아이템의 판매자가 아닙니다.");
        }

        // 환불 상태가 요청된 상태인지 확인
        if (orderItem.getRefundStatus() != RefundStatus.REQUESTED) {
            throw new RuntimeException("현재 환불 요청 상태가 아닙니다.");
        }

        // 환불 상태 변경: REFUNDED
        orderItem.setRefundStatus(RefundStatus.REFUNDED);
        orderItemRepository.save(orderItem);

        // 재고 복원 (필요한 경우)
        item.addStock(orderItem.getCount());
        itemRepository.save(item);

        // (필요시) 주문 상태 또는 결제 상태 업데이트 등 추가 처리 가능
    }

    /**
     * 환불 완료된 주문 아이템 조회 메서드
     */
    @Transactional(readOnly = true)
    public List<RefundRequestDetailDto> getCompletedRefunds(Long sellerId) {
        // 1. 판매자가 등록한 상품 id 목록 조회
        List<Item> sellerItems = itemService.getItemsBySellerId(String.valueOf(sellerId));
        Set<String> sellerItemIds = sellerItems.stream()
                .map(Item::getId)
                .collect(Collectors.toSet());

        // 2. 모든 주문 조회 (실제 운영 환경에선 조건절로 최적화 권장)
        List<Order> allOrders = orderRepository.findAll();

        // 3. 결과 담을 리스트 초기화
        List<RefundRequestDetailDto> result = new ArrayList<>();

        // 4. 각 주문별로 주문자(buyer), 주문 아이템 순회
        for (Order order : allOrders) {
            Buyer buyer = order.getBuyer();

            for (OrderItem oi : order.getOrderItems()) {
                // 주문아이템에 매핑된 배송 정보와 주소 획득
                Delivery delivery = oi.getDelivery();
                Address address = (delivery != null) ? delivery.getAddress() : null;

                // 5. 환불 완료 상태이고, 판매자 상품 목록에 포함된 아이템만 필터링
                if (oi.getRefundStatus() == RefundStatus.REFUNDED && sellerItemIds.contains(oi.getItemId())) {
                    // 6. 아이템 정보 조회
                    Item item = itemService.getItemById(oi.getItemId());
                    // 7. refundRequest 정보 가져오기
                    RefundRequest refundRequest = oi.getRefundRequest();

                    // 8. 내부 DTO 객체 생성
                    BuyerInfo buyerInfo = new BuyerInfo(buyer);
                    AddressInfo addressInfo = (address != null) ? new AddressInfo(address) : null;
                    RefundInfo refundInfo = new RefundInfo(refundRequest);

                    // 9. DTO 변환 후 결과에 추가
                    result.add(new RefundRequestDetailDto(order, oi, item, buyerInfo, addressInfo, refundInfo));
                }
            }
        }
        return result;
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
