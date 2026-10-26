package com.coding.razorpay.payment_service.gateway.adapter;

import com.coding.razorpay.common_lib.dto.PaymentProcessorResponse;
import com.coding.razorpay.common_lib.enums.PaymentMethod;
import com.coding.razorpay.payment_service.gateway.PaymentAdapter;
import com.coding.razorpay.payment_service.gateway.dto.request.PaymentRequest;
import com.coding.razorpay.payment_service.gateway.dto.request.PaymentResult;
import com.coding.razorpay.payment_service.processor.PaymentProcessorRouter;
import com.coding.razorpay.common_lib.dto.PaymentProcessorRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class NetBankingAdapter implements PaymentAdapter {

    private final PaymentProcessorRouter paymentProcessorRouter;

    @Override
    public PaymentResult initiate(PaymentRequest request) {
        log.info("Initiate Payment with NetBanking, PaymentId :" +request.paymentId());

        try {
            PaymentProcessorRequest paymentProcessorRequest = PaymentProcessorRequest.nonCard(
                    request.paymentId(),
                    PaymentMethod.NETBANKING,
                    request.amount(),
                    request.methodDetails()
            );

            PaymentProcessorResponse paymentProcessorResponse = paymentProcessorRouter.charge(paymentProcessorRequest);

            return switch (paymentProcessorResponse) {
                case PaymentProcessorResponse.Failure failure ->
                        new PaymentResult.Failure(failure.errorCode(), failure.errorDescription());

                case PaymentProcessorResponse.Pending pending ->
                        new PaymentResult.Pending(pending.processorReference());

                case PaymentProcessorResponse.Success success -> new PaymentResult.Success(success.bankReference());

            };
        }catch (Exception e){
            log.warn("NetBanking failed, paymentId: {} ", request.paymentId());
            return new PaymentResult.Failure("NBK_FAILED", e.getMessage());
        }

    }

    @Override
    public PaymentResult capture(UUID paymentId) {
        return new PaymentResult.Success("NET_SUCCESS");
    }
}
