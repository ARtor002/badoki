package ir.artor.badoki.security;

import ir.artor.badoki.model.User;
import ir.artor.badoki.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                Claims claims = jwtService.parse(header.substring(7));
                Long userId = Long.valueOf(claims.getSubject());
                User user = userRepository.findById(userId).orElse(null);
                if (user != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    List<SimpleGrantedAuthority> authorities = switch (user.getRole()) {
                        case ADMIN -> List.of(new SimpleGrantedAuthority("ROLE_ADMIN"),
                                new SimpleGrantedAuthority("ROLE_USER"));
                        case DOCTOR -> List.of(new SimpleGrantedAuthority("ROLE_DOCTOR"),
                                new SimpleGrantedAuthority("ROLE_USER"));
                        default -> List.of(new SimpleGrantedAuthority("ROLE_USER"));
                    };
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(user, null, authorities);
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception ignored) {
                // توکن نامعتبر — بدون احراز هویت ادامه می‌دهیم
            }
        }
        filterChain.doFilter(request, response);
    }
}
