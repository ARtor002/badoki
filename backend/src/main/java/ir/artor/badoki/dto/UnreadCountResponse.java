package ir.artor.badoki.dto;

public class UnreadCountResponse {

    private long count;

    public UnreadCountResponse(long count) {
        this.count = count;
    }

    public UnreadCountResponse() {
    }

    public long getCount() {
        return count;
    }
}
