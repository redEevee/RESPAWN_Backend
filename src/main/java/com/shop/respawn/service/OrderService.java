package com.shop.respawn.service;

import com.shop.respawn.domain.*;
import com.shop.respawn.dto.OrderRequestDto;
import com.shop.respawn.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final BuyerRepository buyerRepository;
    private final ItemRepository itemRepository;
    private final AddressRepository addressRepository;  // 추가
    private final CartService cartService;

    /**
     * 장바구니 전체 상품 주문
     */
    public Long orderFromCart(Long buyerId, OrderRequestDto orderRequest) {
        Buyer buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("구매자를 찾을 수 없습니다"));

        Cart cart = cartRepository.findByBuyerId(buyerId)
                .orElseThrow(() -> new RuntimeException("장바구니가 비어있습니다"));

        if (cart.getCartItems().isEmpty()) {
            throw new RuntimeException("장바구니에 상품이 없습니다");
        }

        // 재고 확인
        validateStock(cart.getCartItems());

        // 기존 주소 ID로 Delivery 생성
        Delivery delivery = createDeliveryWithAddressId(buyerId, orderRequest.getAddressId());

        // CartItem을 OrderItem으로 변환
        List<OrderItem> orderItems = cart.getCartItems().stream()
                .map(this::convertCartItemToOrderItem)
                .toList();

        OrderItem[] orderItemArray = orderItems.toArray(new OrderItem[0]);

        // Order 생성
        Order order = Order.createOrder(buyer, delivery, orderItemArray);

        // 주문 저장 (ID 생성을 위해)
        Order savedOrder = orderRepository.save(order);

        // 토스페이먼츠 관련 정보 설정
        setTossPaymentInfo(savedOrder, cart.getCartItems());

        // 재고 차감
        reduceStock(cart.getCartItems());

        // 최종 저장
        savedOrder = orderRepository.save(savedOrder);

        cartService.clearCart(buyerId);
        return savedOrder.getId();
    }

    /**
     * 장바구니 선택 상품 주문
     */
    public Long orderSelectedFromCart(Long buyerId, OrderRequestDto orderRequest) {
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

        // 재고 확인
        validateStock(selectedCartItems);

        // 기존 주소 ID로 Delivery 생성
        Delivery delivery = createDeliveryWithAddressId(buyerId, orderRequest.getAddressId());

        // CartItem을 OrderItem으로 변환
        List<OrderItem> orderItems = selectedCartItems.stream()
                .map(this::convertCartItemToOrderItem)
                .toList();

        OrderItem[] orderItemArray = orderItems.toArray(new OrderItem[0]);

        // Order 생성
        Order order = Order.createOrder(buyer, delivery, orderItemArray);

        // 주문 저장 (ID 생성을 위해)
        Order savedOrder = orderRepository.save(order);

        // 토스페이먼츠 관련 정보 설정
        setTossPaymentInfo(savedOrder, selectedCartItems);

        // 재고 차감
        reduceStock(selectedCartItems);

        // 최종 저장
        savedOrder = orderRepository.save(savedOrder);

        // 선택된 장바구니 아이템들만 제거
        for (CartItem cartItem : selectedCartItems) {
            cart.getCartItems().remove(cartItem);
        }
        cartRepository.save(cart);

        return savedOrder.getId();
    }

    /**
     * 토스페이먼츠 관련 정보 설정
     */
    private void setTossPaymentInfo(Order order, List<CartItem> cartItems) {
        // 총 금액 계산
        int totalAmount = cartItems.stream()
                .mapToInt(CartItem::getTotalPrice)
                .sum();

        // 주문명 생성 (첫 번째 상품명 + 외 N건)
        String orderName = generateOrderName(cartItems);

        // tossOrderId 생성 (주문번호 + 타임스탬프)
        String tossOrderId = "ORDER_" + order.getId() + "_" + System.currentTimeMillis();

        // 토스페이먼츠 필드 설정
        order.setTossOrderId(tossOrderId);
        order.setOrderName(orderName);
        order.setTotalAmount(totalAmount);
        order.setPaymentStatus("READY");
        // paymentKey는 null로 유지 (결제 완료 후 설정)
    }

    /**
     * 주문명 생성 (상품명 기반)
     */
    private String generateOrderName(List<CartItem> cartItems) {
        if (cartItems.isEmpty()) {
            return "상품";
        }

        // 첫 번째 상품 정보 조회
        CartItem firstItem = cartItems.get(0);
        Item item = itemRepository.findById(firstItem.getItemId())
                .orElse(null);

        String firstItemName = (item != null) ? item.getName() : "상품";
        int itemCount = cartItems.size();

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
     * 재고 확인
     */
    private void validateStock(List<CartItem> cartItems) {
        for (CartItem cartItem : cartItems) {
            Item item = itemRepository.findById(cartItem.getItemId())
                    .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다: " + cartItem.getItemId()));

            if (item.getStockQuantity() < cartItem.getCount()) {
                throw new RuntimeException("재고가 부족합니다. 상품: " + item.getName() +
                        ", 요청수량: " + cartItem.getCount() +
                        ", 현재재고: " + item.getStockQuantity());
            }
        }
    }

    /**
     * 재고 차감
     */
    private void reduceStock(List<CartItem> cartItems) {
        for (CartItem cartItem : cartItems) {
            Item item = itemRepository.findById(cartItem.getItemId())
                    .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다: " + cartItem.getItemId()));

            item.removeStock(cartItem.getCount());
            itemRepository.save(item);
        }
    }

    private OrderItem convertCartItemToOrderItem(CartItem cartItem) {
        OrderItem orderItem = new OrderItem();
        orderItem.setItemId(cartItem.getItemId());
        orderItem.setOrderPrice(cartItem.getCartPrice());  // 장바구니 가격을 주문 가격으로
        orderItem.setCount(cartItem.getCount());
        return orderItem;
    }
}
