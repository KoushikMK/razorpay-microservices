package com.coding.razorpay.merchant_service.mapper;


import com.coding.razorpay.merchant_service.dto.response.ApiKeyCreateResponse;
import com.coding.razorpay.merchant_service.dto.response.ApiKeyResponse;
import com.coding.razorpay.merchant_service.entity.ApiKey;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ApiKeyMapper {
    ApiKeyCreateResponse toCreateApiResponse(ApiKey apiKey);

    List<ApiKeyResponse> toResponseList(List<ApiKey> apiKeys);
}
