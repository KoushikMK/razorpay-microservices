package com.coding.razorpay.payment_service.gateway;

import com.coding.razorpay.common_lib.enums.PaymentMethod;
import com.coding.razorpay.payment_service.gateway.dto.request.PaymentRequest;
import com.coding.razorpay.payment_service.gateway.dto.request.PaymentResult;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class PaymentGatewayRouter {

    private Map<PaymentMethod,PaymentAdapter> paymentAdapters;

    public PaymentGatewayRouter(Map<PaymentMethod, PaymentAdapter> paymentAdapters) {
        this.paymentAdapters = paymentAdapters;
    }

    public PaymentResult initiate(PaymentRequest request){
        PaymentAdapter adapter=paymentAdapters.get(request.method());  //ex paymentAdapters.get(PaymentMethod.CARD) -> new CardPaymentAdapter()
        if (adapter == null) {
            throw new IllegalArgumentException("No payment adapter registered for method: "+request.method());
        }
        return adapter.initiate(request);
    }

    public PaymentResult capture(PaymentMethod method, UUID paymentId) {
        PaymentAdapter adapter=paymentAdapters.get(method);  //ex paymentAdapters.get(PaymentMethod.CARD) -> new CardPaymentAdapter()
        if (adapter == null) {
            throw new IllegalArgumentException("No payment adapter registered for method: "+method);
        }
        return adapter.capture(paymentId);
    }

}
