package com.coding.razorpay.payment_service.dto.request;

import com.coding.razorpay.common_lib.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record PaymentInitRequest(
        @NotNull(message = "Order Id is required")
        UUID orderId,

        @NotNull(message = "Payment Id is required")
        PaymentMethod method,

        Map<String,Object> methodDetails
) {
}
