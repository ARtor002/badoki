package ir.artor.badoki.dto;

/** یک بازه زمانی خالی یا پر برای رزرو */
public class SlotDto {

    private String time;
    private boolean available;

    public SlotDto() {}

    public SlotDto(String time, boolean available) {
        this.time = time;
        this.available = available;
    }

    public String getTime() { return time; }
    public boolean isAvailable() { return available; }
}
