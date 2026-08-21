package ir.artor.badoki.dto;


public class OtpResponse {

    private String message;
    private String devOtp;

    public OtpResponse(String message, String devOtp) {
        this.message = message;
        this.devOtp = devOtp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDevOtp() {
        return devOtp;
    }

    public void setDevOtp(String devOtp) {
        this.devOtp = devOtp;
    }

    public OtpResponse() {
    }
}
