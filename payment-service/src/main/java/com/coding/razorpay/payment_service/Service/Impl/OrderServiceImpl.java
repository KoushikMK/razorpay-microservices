package com.coding.razorpay.payment_service.Service.Impl;

import com.coding.razorpay.common_lib.dto.FindOrCreateCustomerRequest;
import com.coding.razorpay.common_lib.enums.EventAggregateType;
import com.coding.razorpay.common_lib.enums.OrderStatus;
import com.coding.razorpay.common_lib.exception.BusinessRuleViolationException;
import com.coding.razorpay.common_lib.exception.DuplicateResourceException;
import com.coding.razorpay.common_lib.exception.ResourceNotFoundException;
import com.coding.razorpay.payment_service.Mapper.OrderMapper;
import com.coding.razorpay.payment_service.Mapper.PaymentMapper;
import com.coding.razorpay.payment_service.Repository.OrderRepository;
import com.coding.razorpay.payment_service.Repository.PaymentRepository;
import com.coding.razorpay.payment_service.Service.OrderService;
import com.coding.razorpay.payment_service.client.CustomerServiceClient;
import com.coding.razorpay.payment_service.dto.request.CreateOrderRequest;
import com.coding.razorpay.payment_service.dto.response.OrderResponse;
import com.coding.razorpay.payment_service.dto.response.PaymentResponse;
import com.coding.razorpay.payment_service.entity.OrderRecord;
import com.coding.razorpay.payment_service.entity.Payment;
import com.coding.razorpay.payment_service.outbox.OutboxEventPublisher;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;
    private final CustomerServiceClient customerServiceClient;
    private final OutboxEventPublisher eventPublisher;
    private final OrderPersistenceService orderPersistenceService;

    @Value("${payment.order.default-order-expiry-minutes:30}")
    private int defaultOrderExpiryMinutes;

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @CircuitBreaker(name = "merchant-service")
    @Retry(name = "merchant-service")
    public OrderResponse create(UUID merchantId, CreateOrderRequest request) {
        if (request.receipt() != null && orderRepository.existsByMerchantIdAndReceipt(merchantId, request.receipt())) {
            throw new DuplicateResourceException("ORDER_RECEIPT_DUPLICATE", "Order with receipt already exists: " + request.receipt());
        }

        UUID customerId = null;
        if (request.customer() != null) {
            customerId = customerServiceClient.findOrCreate(
                    new FindOrCreateCustomerRequest(merchantId,
                            request.customer().email(),
                            request.customer().name(),
                            request.customer().phone())
            );
        }

        return orderPersistenceService.persist(merchantId, request, customerId, defaultOrderExpiryMinutes);
    }

    @Override
    public OrderResponse getById(UUID merchantId, UUID orderId) {
        OrderRecord order = orderRepository.findByIdAndMerchantId(orderId,merchantId)
                .orElseThrow(()->new ResourceNotFoundException("order", orderId));
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancel(UUID merchantId, UUID orderId) {
        OrderRecord order = orderRepository.findByIdAndMerchantId(orderId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if(order.getOrderStatus()==OrderStatus.CANCELLED|| order.getOrderStatus()==OrderStatus.PAID){
            throw new BusinessRuleViolationException("ORDER_CANNOT_CANCEL","cannot cancel this order" +order.getOrderStatus().name());
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        order=orderRepository.save(order);

        eventPublisher.publish(EventAggregateType.ORDER, order.getId(), "ORDER_CANCELLED",
                Map.of("orderId", order.getId(),
                        "merchantId", merchantId.toString(),
                        "orderStatus", order.getOrderStatus().name(),
                        "amountUnits", order.getAmount().getAmountUnits(),
                        "amountCurrency", order.getAmount().getCurrency()
                )
        );
        return orderMapper.toResponse(order);
    }

    @Override
    public List<PaymentResponse> listPayments(UUID merchantId, UUID orderId) {
        OrderRecord order = orderRepository.findByIdAndMerchantId(orderId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        List<Payment> paymentList=paymentRepository.findByOrder_Id(order);
        return paymentMapper.toResponseList(paymentList);
    }
}
