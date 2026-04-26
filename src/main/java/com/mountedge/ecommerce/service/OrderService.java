package com.mountedge.ecommerce.service;

import com.mountedge.ecommerce.dto.OrderSummaryDto;
import com.mountedge.ecommerce.dto.OrderDetailDto;
import com.mountedge.ecommerce.entity.*;
import com.mountedge.ecommerce.exception.BadRequestException;
import com.mountedge.ecommerce.exception.ResourceNotFoundException;
import com.mountedge.ecommerce.mapper.OrderMapper;
import com.mountedge.ecommerce.repository.AddressRepository;
import com.mountedge.ecommerce.repository.OrderRepository;
import com.mountedge.ecommerce.repository.OrderStatusHistoryRepository;
import com.mountedge.ecommerce.repository.PaymentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final CartService cartService;
    private final AddressRepository addressRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final OrderMapper orderMapper;
    private final BulkDiscountService bulkDiscountService;
    private final com.mountedge.ecommerce.repository.ProductRepository productRepository;
    private final com.mountedge.ecommerce.repository.UserRepository userRepository;

    public OrderService(OrderRepository orderRepository, PaymentRepository paymentRepository, CartService cartService, AddressRepository addressRepository, OrderStatusHistoryRepository historyRepository, OrderMapper orderMapper, BulkDiscountService bulkDiscountService, com.mountedge.ecommerce.repository.ProductRepository productRepository, com.mountedge.ecommerce.repository.UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.cartService = cartService;
        this.addressRepository = addressRepository;
        this.historyRepository = historyRepository;
        this.orderMapper = orderMapper;
        this.bulkDiscountService = bulkDiscountService;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public OrderSummaryDto checkout(Long userId, Long addressId, String paymentMethodStr) {
        Cart cart = cartService.getCartByUserId(userId);
        
        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        
        if (!address.getUser().getUserId().equals(userId)) {
            throw new BadRequestException("Invalid address for user");
        }

        PaymentMethod paymentMethod;
        try {
            paymentMethod = PaymentMethod.valueOf(paymentMethodStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid payment method");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalDiscountAmount = BigDecimal.ZERO;
        boolean isBulkOrder = false;
        
        // Use a temporary list to hold order items until we create the order
        List<OrderItem> tempItems = new java.util.ArrayList<>();

        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (product.getInventory() == null || product.getInventory().getStockQuantity() < item.getQuantity()) {
                throw new BadRequestException("Not enough stock for: " + product.getName());
            }
            // Reduce stock
            product.getInventory().setStockQuantity(product.getInventory().getStockQuantity() - item.getQuantity());
            
            BigDecimal originalPrice = product.getPrice();
            BigDecimal discountedPrice = originalPrice; // No bulk discount in regular checkout
            
            BigDecimal itemDiscountedTotal = discountedPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            
            totalAmount = totalAmount.add(itemDiscountedTotal);
            
            OrderItem orderItem = new OrderItem(null, product, item.getQuantity(), discountedPrice, originalPrice);
            tempItems.add(orderItem);
        }

        Order order = new Order(cart.getUser(), address, paymentMethod, totalAmount, OrderStatus.PLACED);
        order.setIsBulkOrder(false);

        for (OrderItem oi : tempItems) {
            order.addItem(oi);
        }

        OrderStatusHistory history = new OrderStatusHistory(order, OrderStatus.PLACED, "Order placed successfully");
        order.addStatusHistory(history);

        Order savedOrder = orderRepository.save(order);
        
        String transactionRef = paymentMethod == PaymentMethod.COD ? "COD-" + savedOrder.getOrderId() : "ONL-" + System.currentTimeMillis();
        Payment payment = new Payment(savedOrder, paymentMethod == PaymentMethod.COD ? "PENDING" : "SUCCESS", transactionRef);
        paymentRepository.save(payment);

        cartService.clearCart(cart);

        return orderMapper.toSummaryDto(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderDetailDto getOrderDetails(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        OrderDetailDto dto = new OrderDetailDto();
        dto.setOrderId(order.getOrderId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus().name());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setPaymentMethod(order.getPaymentMethod().name());
        dto.setShippingAddress(String.format("%s, %s, %s - %s", 
                order.getShippingAddress().getAddressLine1(), 
                order.getShippingAddress().getCity(), 
                order.getShippingAddress().getState(), 
                order.getShippingAddress().getPincode()));
        dto.setCustomerName(order.getUser().getName());
        dto.setCustomerEmail(order.getUser().getEmail());
        dto.setIsBulkOrder(order.getIsBulkOrder());
        dto.setDiscountPercentage(order.getDiscountPercentage());
        dto.setDiscountAmount(order.getDiscountAmount());

        dto.setItems(order.getItems().stream()
                .map(i -> new OrderDetailDto.OrderItemDetailDto(
                        i.getProduct().getName(),
                        i.getQuantity(),
                        i.getPrice(),
                        i.getOriginalPrice()))
                .collect(java.util.stream.Collectors.toList()));

        return dto;
    }

    @Transactional(readOnly = true)
    public Page<OrderSummaryDto> getUserOrders(Long userId, Pageable pageable) {
        // Find all orders for the user (ignoring pagination for simplicity since we added the method to return List)
        // A proper implementation would use findByUserUserIdOrderByCreatedAtDesc with Pageable.
        // But since we just added List<Order>, we'll return a manual page or convert it.
        // Actually, let's just convert the list and wrap it.
        List<Order> orders = orderRepository.findByUserUserIdOrderByCreatedAtDesc(userId);
        return new org.springframework.data.domain.PageImpl<>(
                orders.stream().map(orderMapper::toSummaryDto).collect(java.util.stream.Collectors.toList()),
                pageable,
                orders.size()
        );
    }

    @Transactional
    public OrderSummaryDto updateOrderStatus(Long orderId, String statusStr, String notes) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid order status");
        }

        order.setStatus(newStatus);
        OrderStatusHistory history = new OrderStatusHistory(order, newStatus, notes);
        order.addStatusHistory(history);

        if (newStatus == OrderStatus.CANCELLED) {
            // Restore stock
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                if (product.getInventory() != null) {
                    product.getInventory().setStockQuantity(product.getInventory().getStockQuantity() + item.getQuantity());
                }
            }
        }

        Order savedOrder = orderRepository.save(order);
        return orderMapper.toSummaryDto(savedOrder);
    }

    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getBulkOrderPreview(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found")); 
        
        BigDecimal originalPrice = product.getPrice();
        BigDecimal discountPct = bulkDiscountService.calculateDiscountPct(quantity);
        
        BigDecimal discountedPrice = originalPrice;
        if (discountPct.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discountFactor = BigDecimal.ONE.subtract(discountPct.divide(new BigDecimal("100")));
            discountedPrice = originalPrice.multiply(discountFactor);
        }
        
        BigDecimal itemOriginalTotal = originalPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal itemDiscountedTotal = discountedPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal totalDiscount = itemOriginalTotal.subtract(itemDiscountedTotal);
        
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("productId", product.getProductId());
        response.put("name", product.getName());
        response.put("quantity", quantity);
        response.put("originalPrice", originalPrice);
        response.put("discountPct", discountPct);
        response.put("discountedPrice", discountedPrice);
        response.put("totalOriginal", itemOriginalTotal);
        response.put("totalDiscount", totalDiscount);
        response.put("finalTotal", itemDiscountedTotal);
        return response;
    }

    @Transactional
    public OrderSummaryDto requestBulkOrder(Long userId, Long productId, int quantity, Long addressId, String paymentMethodStr) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        
        if (!address.getUser().getUserId().equals(userId)) {
            throw new BadRequestException("Invalid address for user");
        }

        PaymentMethod paymentMethod;
        try {
            paymentMethod = PaymentMethod.valueOf(paymentMethodStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid payment method");
        }

        BigDecimal originalPrice = product.getPrice();
        BigDecimal discountPct = bulkDiscountService.calculateDiscountPct(quantity);
        BigDecimal discountedPrice = originalPrice;
        
        if (discountPct.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discountFactor = BigDecimal.ONE.subtract(discountPct.divide(new BigDecimal("100")));
            discountedPrice = originalPrice.multiply(discountFactor);
        }
        
        BigDecimal itemOriginalTotal = originalPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal itemDiscountedTotal = discountedPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal totalDiscount = itemOriginalTotal.subtract(itemDiscountedTotal);

        Order order = new Order(user, address, paymentMethod, itemDiscountedTotal, OrderStatus.PENDING_APPROVAL);
        order.setIsBulkOrder(true);
        order.setDiscountPercentage(discountPct);
        order.setDiscountAmount(totalDiscount);

        OrderItem orderItem = new OrderItem(null, product, quantity, discountedPrice, originalPrice);
        order.addItem(orderItem);

        OrderStatusHistory history = new OrderStatusHistory(order, OrderStatus.PENDING_APPROVAL, "Bulk order requested.");
        order.addStatusHistory(history);

        Order savedOrder = orderRepository.save(order);
        
        // Let's create a pending payment
        String transactionRef = paymentMethod == PaymentMethod.COD ? "COD-BLK-" + savedOrder.getOrderId() : "ONL-BLK-" + System.currentTimeMillis();
        Payment payment = new Payment(savedOrder, "PENDING", transactionRef);
        paymentRepository.save(payment);

        return orderMapper.toSummaryDto(savedOrder);
    }

    @Transactional
    public OrderSummaryDto approveBulkOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (order.getStatus() != OrderStatus.PENDING_APPROVAL) {
            throw new BadRequestException("Order is not pending approval");
        }
        
        // Deduct inventory at approval time instead of request time
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (product.getInventory() == null || product.getInventory().getStockQuantity() < item.getQuantity()) {
                throw new BadRequestException("Not enough stock for: " + product.getName() + " to approve this bulk order");
            }
            product.getInventory().setStockQuantity(product.getInventory().getStockQuantity() - item.getQuantity());
        }

        order.setStatus(OrderStatus.CONFIRMED);
        OrderStatusHistory history = new OrderStatusHistory(order, OrderStatus.CONFIRMED, "Bulk order approved by admin.");
        order.addStatusHistory(history);

        Order savedOrder = orderRepository.save(order);
        return orderMapper.toSummaryDto(savedOrder);
    }

    @Transactional
    public OrderSummaryDto rejectBulkOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (order.getStatus() != OrderStatus.PENDING_APPROVAL) {
            throw new BadRequestException("Order is not pending approval");
        }

        order.setStatus(OrderStatus.REJECTED);
        OrderStatusHistory history = new OrderStatusHistory(order, OrderStatus.REJECTED, "Bulk order rejected by admin.");
        order.addStatusHistory(history);

        Order savedOrder = orderRepository.save(order);
        return orderMapper.toSummaryDto(savedOrder);
    }

    @Transactional
    public Order checkoutScheduledOrder(ScheduledOrder scheduledOrder) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalDiscountAmount = BigDecimal.ZERO;
        boolean isBulkOrder = false;
        
        List<OrderItem> tempItems = new java.util.ArrayList<>();

        for (ScheduledOrderItem item : scheduledOrder.getItems()) {
            Product product = item.getProduct();
            if (product.getInventory() == null || product.getInventory().getStockQuantity() < item.getQuantity()) {
                throw new BadRequestException("Not enough stock for: " + product.getName());
            }
            // Reduce stock
            product.getInventory().setStockQuantity(product.getInventory().getStockQuantity() - item.getQuantity());
            
            BigDecimal originalPrice = product.getPrice();
            BigDecimal discountPct = bulkDiscountService.calculateDiscountPct(item.getQuantity());
            BigDecimal discountedPrice = originalPrice;
            
            if (discountPct.compareTo(BigDecimal.ZERO) > 0) {
                isBulkOrder = true;
                BigDecimal discountFactor = BigDecimal.ONE.subtract(discountPct.divide(new BigDecimal("100")));
                discountedPrice = originalPrice.multiply(discountFactor);
            }
            
            BigDecimal itemOriginalTotal = originalPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            BigDecimal itemDiscountedTotal = discountedPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            
            totalAmount = totalAmount.add(itemDiscountedTotal);
            totalDiscountAmount = totalDiscountAmount.add(itemOriginalTotal.subtract(itemDiscountedTotal));
            
            OrderItem orderItem = new OrderItem(null, product, item.getQuantity(), discountedPrice, originalPrice);
            tempItems.add(orderItem);
        }

        Order order = new Order(scheduledOrder.getUser(), scheduledOrder.getShippingAddress(), scheduledOrder.getPaymentMethod(), totalAmount, OrderStatus.PLACED);
        order.setIsBulkOrder(isBulkOrder);
        if(isBulkOrder) {
            order.setDiscountAmount(totalDiscountAmount);
        }

        for (OrderItem oi : tempItems) {
            order.addItem(oi);
        }

        OrderStatusHistory history = new OrderStatusHistory(order, OrderStatus.PLACED, "Scheduled Order processed automatically.");
        order.addStatusHistory(history);

        Order savedOrder = orderRepository.save(order);
        
        String transactionRef = scheduledOrder.getPaymentMethod() == PaymentMethod.COD ? "COD-SCH-" + savedOrder.getOrderId() : "ONL-SCH-" + System.currentTimeMillis();
        Payment payment = new Payment(savedOrder, scheduledOrder.getPaymentMethod() == PaymentMethod.COD ? "PENDING" : "SUCCESS", transactionRef);
        paymentRepository.save(payment);

        return savedOrder;
    }

    @Transactional(readOnly = true)
    public List<com.mountedge.ecommerce.dto.BulkOrderDto> getAdminBulkOrders() {
        return orderRepository.findAll().stream()
                .filter(o -> Boolean.TRUE.equals(o.getIsBulkOrder()))
                .map(o -> {
                    int totalItems = o.getItems().stream().mapToInt(OrderItem::getQuantity).sum();
                    BigDecimal originalTotal = o.getTotalAmount().add(o.getDiscountAmount());
                    return new com.mountedge.ecommerce.dto.BulkOrderDto(
                            o.getOrderId(),
                            o.getUser().getName(),
                            o.getUser().getEmail(),
                            totalItems,
                            o.getDiscountAmount(),
                            originalTotal,
                            o.getTotalAmount(),
                            o.getStatus().name(),
                            o.getCreatedAt()
                    );
                })
                .collect(java.util.stream.Collectors.toList());
    }
}
