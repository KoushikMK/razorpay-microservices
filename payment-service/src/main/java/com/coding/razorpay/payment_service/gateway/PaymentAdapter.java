package com.coding.razorpay.payment_service.gateway;


import com.coding.razorpay.payment_service.gateway.dto.request.PaymentRequest;
import com.coding.razorpay.payment_service.gateway.dto.request.PaymentResult;

import java.util.UUID;

public interface PaymentAdapter {

    public PaymentResult initiate(PaymentRequest paymentRequest);

    public PaymentResult capture(UUID paymentId);
}
