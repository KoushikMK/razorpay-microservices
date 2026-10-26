package com.coding.razorpay.merchant_service.dto.response;


import com.coding.razorpay.common_lib.enums.BusinessType;
import com.coding.razorpay.common_lib.enums.MerchantStatus;

import java.util.UUID;

public record MerchantResponse(
        UUID id,
        String name,
        String email,
        String businessName,
        BusinessType businessType,
        MerchantStatus merchantStatus
) {
}
