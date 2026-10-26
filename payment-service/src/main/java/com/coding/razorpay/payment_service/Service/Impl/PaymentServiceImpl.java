package com.coding.razorpay.payment_service.Service.Impl;


import com.coding.razorpay.common_lib.enums.EventAggregateType;
import com.coding.razorpay.common_lib.enums.OrderStatus;
import com.coding.razorpay.common_lib.enums.PaymentEvent;
import com.coding.razorpay.common_lib.enums.PaymentStatus;
import com.coding.razorpay.common_lib.exception.ResourceNotFoundException;
import com.coding.razorpay.payment_service.Mapper.PaymentMapper;
import com.coding.razorpay.payment_service.Repository.OrderRepository;
import com.coding.razorpay.payment_service.Repository.PaymentRepository;
import com.coding.razorpay.payment_service.Service.PaymentService;
import com.coding.razorpay.payment_service.dto.request.PaymentInitRequest;
import com.coding.razorpay.payment_service.dto.response.PaymentResponse;
import com.coding.razorpay.payment_service.entity.OrderRecord;
import com.coding.razorpay.payment_service.entity.Payment;
import com.coding.razorpay.payment_service.gateway.PaymentGatewayRouter;
import com.coding.razorpay.payment_service.gateway.dto.request.PaymentRequest;
import com.coding.razorpay.payment_service.gateway.dto.request.PaymentResult;
import com.coding.razorpay.payment_service.outbox.OutboxEventPublisher;
import com.coding.razorpay.payment_service.saga.PaymentAuthorizationRecorder;
import com.coding.razorpay.payment_service.stateMachine.PaymentTransitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentGatewayRouter paymentGatewayRouter;
    private final PaymentMapper paymentMapper;
    private final PaymentTransitionService paymentTransitionService;
    private final OutboxEventPublisher eventPublisher;
    private final PaymentAuthorizationRecorder paymentAuthorizationRecorder;

    @Override
    public PaymentResponse initiate(UUID merchantId, PaymentInitRequest request,String idempotencyKey) {

        if (idempotencyKey != null) {
            var existing = paymentAuthorizationRecorder.findExistingAttempt(merchantId, idempotencyKey);
            if (existing.isPresent()) {
                log.info("Idempotency replay for paymentId: {}", existing.get().id());
                return existing.get();
            }
        }

        Payment payment = paymentAuthorizationRecorder.recordPayment(merchantId,request,idempotencyKey);

        PaymentRequest paymentRequest = new PaymentRequest(payment.getId(),
                request.orderId(), merchantId,
                payment.getAmount(), request.method(),
                request.methodDetails());

        PaymentResult result;
        try {
            result = paymentGatewayRouter.initiate(paymentRequest);
        } catch (Exception e) {
            return paymentAuthorizationRecorder.compensateAuthorizationFailure(payment.getId(), "PAYMENT_GATEWAY_ROUTER_UNREACHABLE", e.getMessage());
        }
        return paymentAuthorizationRecorder.applyGatewayResult(payment.getId(), result);
    }

    @Override
    public PaymentResponse capture(UUID merchantId,UUID paymentId) {
//        Payment payment=paymentRepository.findByIdAndMerchantId(merchantId,paymentId)
//                .orElseThrow(()->new ResourceNotFoundException("Payment",paymentId));

        Payment payment=paymentRepository.findByIdAndMerchantIdForUpdate(merchantId,paymentId)
                .orElseThrow(()->new ResourceNotFoundException("Payment",paymentId));

//        payment.setStatus(PaymentStatus.CAPTURING);
        paymentTransitionService.apply(payment,PaymentEvent.CAPTURE_REQUEST);

        PaymentResult paymentResult=paymentGatewayRouter.capture(payment.getMethod(),paymentId);

        if(paymentResult instanceof PaymentResult.Success success){
//            payment.setStatus(PaymentStatus.CAPTURED);
            paymentTransitionService.apply(payment,PaymentEvent.CAPTURE_SUCCESS);
            payment.setCapturedAt(LocalDateTime.now());
            log.info("Payment captured, paymentId: {}" ,paymentId);
        } else if (paymentResult instanceof PaymentResult.Failure failure) {
//            payment.setStatus(PaymentStatus.AUTHORIZED);
            paymentTransitionService.apply(payment,PaymentEvent.CAPTURE_FAIL);
            payment.setErrorCode(failure.errorCode());
            payment.setErrorDescription(failure.errorDescription());
            log.warn("Payment capture failure, paymentId: {}" ,paymentId);
        }
        payment=paymentRepository.save(payment);

        eventPublisher.publish(EventAggregateType.PAYMENT, payment.getId(), "PAYMENT_STATUS_CHANGED",
                Map.of("orderId", payment.getOrder().getId().toString(),
                        "paymentId", payment.getId().toString(),
                        "merchantId", merchantId.toString(),
                        "paymentStatus", payment.getStatus().name(),
                        "amountUnits", payment.getAmount().getAmountUnits(),
                        "amountCurrency", payment.getAmount().getCurrency(),
                        "paymentMethod", payment.getMethod()
                )
        );

        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public void resolveAuthorization(UUID paymentId, boolean approve, String bankRef, String errorCode, String errorDescription) {

//        Payment payment=paymentRepository.findById(paymentId)
//                .orElseThrow(()->new ResourceNotFoundException("Payment",paymentId));

        Payment payment=paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(()->new ResourceNotFoundException("Payment",paymentId));

        if (payment.getStatus() != PaymentStatus.AUTHORIZING) {
            log.warn("Payment is not in Authorizing state, paymentID: {}, status: {}", paymentId, payment.getStatus());
            return;
        }

        OrderRecord orderRecord = payment.getOrder();

        if (approve) {
            paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_SUCCESS);
            payment.setBankReference(bankRef);
            payment.setAuthorizedAt(LocalDateTime.now());

            // Auto-capture
            paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_REQUEST);
            PaymentResult captureResult = paymentGatewayRouter.capture(payment.getMethod(), paymentId);

            if(captureResult instanceof PaymentResult.Success success) {
                paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_SUCCESS);
                payment.setCapturedAt(LocalDateTime.now());
                orderRecord.setOrderStatus(OrderStatus.PAID);
            } else if (captureResult instanceof  PaymentResult.Failure failure){
                paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_FAIL);
                payment.setErrorCode(failure.errorCode());
                payment.setErrorDescription(failure.errorDescription());
            }
        }else{
            paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_FAIL);
            payment.setErrorCode(errorCode);
            payment.setErrorDescription(errorDescription);
        }
        paymentRepository.save(payment);
        orderRepository.save(orderRecord);

        eventPublisher.publish(EventAggregateType.PAYMENT, payment.getId(), "PAYMENT_STATUS_CHANGED",
                Map.of("orderId", payment.getOrder().getId().toString(),
                        "paymentId", payment.getId().toString(),
                        "merchantId", payment.getMerchantId().toString(),
                        "paymentStatus", payment.getStatus().name(),
                        "amountUnits", payment.getAmount().getAmountUnits(),
                        "amountCurrency", payment.getAmount().getCurrency(),
                        "paymentMethod", payment.getMethod()
                )
        );


    }
}
