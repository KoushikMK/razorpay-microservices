package com.coding.razorpay.common_lib.context;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;


//@RequestScope(proxyMode = ScopedProxyMode.TARGET_CLASS)  //spring will create only proxcy beans and not able to use this till once you initialize //usese thread local
@Getter
@Setter
public class MerchantContext {

    private UUID merchantId;
    private String keyId;
}
