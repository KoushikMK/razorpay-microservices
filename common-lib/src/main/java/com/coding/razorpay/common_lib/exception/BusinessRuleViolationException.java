package com.coding.razorpay.common_lib.exception;

import lombok.Getter;

@Getter
public class BusinessRuleViolationException extends RuntimeException{

    private final String errorcode;

    public BusinessRuleViolationException(String errorcode,String message){
        super(message);
        this.errorcode=errorcode;
    }

}
