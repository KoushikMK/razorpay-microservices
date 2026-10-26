package com.coding.razorpay.common_lib.exception;

import lombok.Getter;

@Getter
public class DuplicateResourceException extends RuntimeException{

    private final String errorcode;

    public DuplicateResourceException(String errorcode,String message){
        super(message);
        this.errorcode=errorcode;
    }



}
