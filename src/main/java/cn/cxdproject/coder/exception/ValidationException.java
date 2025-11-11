package cn.cxdproject.coder.exception;

import lombok.Getter;

/**
 * User-defined parameter verification exception is used to encapsulate the fields and error messages that failed verification.
 *
 * @author heathcetide
 */
@Getter
public class ValidationException extends RuntimeException {

    /**
     * Error code, used in conjunction with ErrorCodeEnum.
     */
    private final String errorCode;

    /**
     * Name of the field that failed validation.
     */
    private final String field;

    public ValidationException(String errorCode, String message, String field) {
        super(message);
        this.errorCode = errorCode;
        this.field = field;
    }

    @Override
    public String toString() {
        return "ValidationException{" +
                "errorCode='" + errorCode + '\'' +
                ", field='" + field + '\'' +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}
