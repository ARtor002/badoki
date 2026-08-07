package ir.artor.badoki.dto;

import ir.artor.badoki.model.Role;

public class UserResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private Role role;
    private String createdAt;

    public UserResponse() {}

    public UserResponse(Long id, String fullName, String email, String phone, Role role, String createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public Role getRole() { return role; }
    public String getCreatedAt() { return createdAt; }
}
