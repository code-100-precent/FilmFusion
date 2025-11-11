package cn.cxdproject.coder.exception;

/**
 * UnauthorizedException
 *
 * @author heathcetide
 */
public class UnauthorizedException extends RuntimeException {
    private final Integer code;

    public UnauthorizedException(String message) {
        super(message);
        this.code = 500;
    }

    public UnauthorizedException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}