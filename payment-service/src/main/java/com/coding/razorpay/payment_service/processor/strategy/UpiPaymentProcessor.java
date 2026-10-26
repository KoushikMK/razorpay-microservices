package com.coding.razorpay.payment_service.processor.strategy;

import com.coding.razorpay.common_lib.Util.RandomizerUtil;
import com.coding.razorpay.common_lib.dto.PaymentProcessorResponse;
import com.coding.razorpay.payment_service.processor.PaymentProcessor;
import com.coding.razorpay.common_lib.dto.PaymentProcessorRequest;
import org.springframework.stereotype.Component;

@Component
public class UpiPaymentProcessor implements PaymentProcessor {

    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {
        final String VPA_CODE_FAIL = "fail@okaxis";

        String bankCode = request.methodDetails() != null ?
                request.methodDetails().get("vpa").toString() : null;

        // simulation
        if (VPA_CODE_FAIL.equals(bankCode)) {
            return new PaymentProcessorResponse.Failure("UPI_REJECTED",
                    "Banked rejected the transaction registration"
            );
        }

        String processorRef = "UPI_PROCESSOR_"+ RandomizerUtil.randomBase64(16);
//        String bankref="BANK_REF"+RandomizerUtil.randomBase64(16);  // this will give by bank

        return new PaymentProcessorResponse.Pending(processorRef);
    }
}
