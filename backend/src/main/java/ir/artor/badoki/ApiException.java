package ir.artor.badoki;

import org.springframework.http.HttpStatus;

/** خطای تجاری با پیام فارسی که به کلاینت برمی‌گردد */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
