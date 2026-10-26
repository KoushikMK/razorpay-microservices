package com.coding.razorpay.api_gateway_service.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.security")
public class SecurityRouteProperties {

    private List<String> publicRoutes = List.of();   //the array will be injected from yml
}
