package cn.cxdproject.coder.exception;

import lombok.Getter;

/**
 * User's request is legal but cannot be executed due to business rules, such as insufficient inventory and balance.
 *
 * @author heathcetide
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * Error code, used in conjunction with ErrorCodeEnum.
     */
    private final Integer errorCode;

    /**
     * Error message
     */
    private final String errorDetail;

    public BusinessException(Integer errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.errorDetail = null;
    }

    public BusinessException(Integer errorCode, String message, String errorDetail) {
        super(message);
        this.errorCode = errorCode;
        this.errorDetail = errorDetail;
    }

    public BusinessException(Integer errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorDetail = null;
    }

    public BusinessException(Integer errorCode, String message, String errorDetail, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorDetail = errorDetail;
    }

    @Override
    public String toString() {
        return "BusinessException{" +
                "errorCode=" + errorCode +
                ", message='" + getMessage() + '\'' +
                ", errorDetail='" + errorDetail + '\'' +
                '}';
    }
}
