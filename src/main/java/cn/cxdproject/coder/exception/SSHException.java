package cn.cxdproject.coder.exception;

import lombok.Getter;

@Getter
public class SSHException extends RuntimeException {
    private final String sessionId;

    public SSHException(String message, String sessionId) {
        super(message);
        this.sessionId = sessionId;
    }

    public SSHException(String message, String sessionId, Throwable cause) {
        super(message, cause);
        this.sessionId = sessionId;
    }
}