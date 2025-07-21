package com.icbt.pahanaeduonlinebillingsystem.common.exception;

/**
 * @author Thisara Kavishka
 * @date 2025-07-22
 * @since 1.0
 */
public class PahanaEduOnlineBillingSystemException extends RuntimeException {

    private final ExceptionType exceptionType;

    public PahanaEduOnlineBillingSystemException(ExceptionType exceptionType) {
        super(exceptionType.name());
        this.exceptionType = exceptionType;
    }

    public ExceptionType getExceptionType() {
        return exceptionType;
    }
}