package cn.cxdproject.coder.exception;

import lombok.Getter;

/**
 * Encapsulate internal errors of the server, such as abnormal database and failure of third-party service call.
 *
 * @author heathcetide
 */
@Getter
public class SystemException extends RuntimeException {

    /**
     * Error code, used in conjunction with ErrorCodeEnum.
     */
    private final Integer errorCode;

    /**
     * Error message
     */
    private final String errorDetail;

    public SystemException(Integer errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.errorDetail = null;
    }

    public SystemException(Integer errorCode, String message, String errorDetail) {
        super(message);
        this.errorCode = errorCode;
        this.errorDetail = errorDetail;
    }

    public SystemException(Integer errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorDetail = null;
    }

    public SystemException(Integer errorCode, String message, String errorDetail, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorDetail = errorDetail;
    }

    @Override
    public String toString() {
        return "SystemException{" +
                "errorCode=" + errorCode +
                ", message='" + getMessage() + '\'' +
                ", errorDetail='" + errorDetail + '\'' +
                ", cause='" + getCause() + '\'' +
                '}';
    }
}
