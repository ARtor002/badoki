package ir.artor.badoki.service;

import ir.artor.badoki.ApiException;
import ir.artor.badoki.dto.AuthResponse;
import ir.artor.badoki.dto.LoginRequest;
import ir.artor.badoki.dto.RegisterRequest;
import ir.artor.badoki.dto.UserResponse;
import ir.artor.badoki.model.Role;
import ir.artor.badoki.model.User;
import ir.artor.badoki.repository.UserRepository;
import ir.artor.badoki.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "این ایمیل قبلاً ثبت شده است. وارد شوید.");
        }
        User user = new User();
        user.setFullName(req.getFullName().trim());
        user.setEmail(email);
        user.setPhone(req.getPhone());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRole(Role.PATIENT);
        user.setCreatedAt(Instant.now());
        userRepository.save(user);
        return toAuthResponse(user);
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED,
                        "ایمیل یا رمز عبور اشتباه است"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "ایمیل یا رمز عبور اشتباه است");
        }
        return toAuthResponse(user);
    }

    private AuthResponse toAuthResponse(User user) {
        String token = jwtService.generateToken(user);
        UserResponse u = new UserResponse(
                user.getId(), user.getFullName(), user.getEmail(),
                user.getPhone(), user.getRole(),
                user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        return new AuthResponse(token, u);
    }
}
