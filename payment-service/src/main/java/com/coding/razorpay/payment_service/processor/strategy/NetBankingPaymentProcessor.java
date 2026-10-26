package com.coding.razorpay.payment_service.processor.strategy;

import com.coding.razorpay.common_lib.Util.RandomizerUtil;
import com.coding.razorpay.common_lib.dto.PaymentProcessorResponse;
import com.coding.razorpay.payment_service.processor.PaymentProcessor;
import com.coding.razorpay.common_lib.dto.PaymentProcessorRequest;
import org.springframework.stereotype.Component;

@Component
public class NetBankingPaymentProcessor implements PaymentProcessor {
    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {

        final String BANK_CODE_FAIL = "BANK_CODE_FAIL";

        String bankCode = request.methodDetails() != null ?
                request.methodDetails().get("bank").toString() : null;  //client will give canara bank

        // simulation
        if(BANK_CODE_FAIL.equals(bankCode)){
            return new PaymentProcessorResponse.Failure("BANK_REJECTED",
                    "Banked rejected the transaction registration"
            );
        }

        String processorRef = "NBK_PROCESSOR_"+ RandomizerUtil.randomBase64(16);
//        String redirectRef = "http://REDIRECT_BANK.com/"+processorRef;   // this will give by bank

        return new PaymentProcessorResponse.Pending(processorRef);

    }
}
