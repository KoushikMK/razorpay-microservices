package com.coding.razorpay.payment_service.processor;


import com.coding.razorpay.common_lib.dto.PaymentProcessorResponse;
import com.coding.razorpay.common_lib.dto.PaymentProcessorRequest;

public interface PaymentProcessor {

    PaymentProcessorResponse charge(PaymentProcessorRequest request);
}
