package com.coding.razorpay.common_lib.exception;

public class IdempotencyConflictException extends RuntimeException{

    public  IdempotencyConflictException(String message){
        super(message);
    }
}
