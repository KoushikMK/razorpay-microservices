package com.coding.razorpay.merchant_service.dto.request;


import com.coding.razorpay.common_lib.enums.Environment;

public record CreateApiKeyRequest(
        Environment environment
) {
}

