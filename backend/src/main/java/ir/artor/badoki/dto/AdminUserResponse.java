package ir.artor.badoki.dto;

import ir.artor.badoki.model.Role;
import ir.artor.badoki.model.User;

/** کاربر برای نمای ادمین */
public class AdminUserResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private Role role;
    private String createdAt;

    public AdminUserResponse() {}

    public static AdminUserResponse from(User u) {
        AdminUserResponse r = new AdminUserResponse();
        r.id = u.getId();
        r.fullName = u.getFullName();
        r.email = u.getEmail();
        r.phone = u.getPhone();
        r.role = u.getRole();
        r.createdAt = u.getCreatedAt() != null ? u.getCreatedAt().toString() : null;
        return r;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public Role getRole() { return role; }
    public String getCreatedAt() { return createdAt; }
}
