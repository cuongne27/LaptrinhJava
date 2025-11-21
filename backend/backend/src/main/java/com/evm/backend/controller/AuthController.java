package com.evm.backend.controller;

import com.evm.backend.config.JwtTokenProvider;
import com.evm.backend.dto.request.LoginRequest;
import com.evm.backend.dto.request.SignupRequest;
import com.evm.backend.dto.response.JwtResponse;
import com.evm.backend.entity.Role;
import com.evm.backend.entity.RoleType;
import com.evm.backend.entity.User;
import com.evm.backend.repository.RoleRepository;
import com.evm.backend.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth") // <<< MODULE: XÁC THỰC VÀ ĐĂNG KÝ (AUTHENTICATION)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {
    AuthenticationManager authenticationManager;
    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder encoder;
    JwtTokenProvider tokenProvider;

    // <<< CHỨC NĂNG: ĐĂNG NHẬP HỆ THỐNG
    // <<< ĐẦU API: POST /api/auth/login
    // <<< VAI TRÒ: PUBLIC (Không yêu cầu xác thực)
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // Xác thực từ username, password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        // Cập nhật SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Tạo token
        String jwt = tokenProvider.generateToken(authentication);

        // Lấy thông tin UserDetails từ Authentication
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Lấy thông tin user từ database
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        // Lấy roles của user
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Trả về token và thông tin user
        return ResponseEntity.ok(new JwtResponse(
                jwt,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roles
        ));
    }

    // <<< CHỨC NĂNG: ĐĂNG KÝ TÀI KHOẢN MỚI
    // <<< ĐẦU API: POST /api/auth/sign-up
    // <<< VAI TRÒ: PUBLIC (Không yêu cầu xác thực)
    @PostMapping("/sign-up")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        // Kiểm tra username đã tồn tại chưa
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body("Lỗi: Username đã được sử dụng!");
        }

        // Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body("Lỗi: Email đã được sử dụng!");
        }

        // Tạo account mới
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setFullName(signUpRequest.getFullName());
        user.setEmail(signUpRequest.getEmail());
        user.setPasswordHash(encoder.encode(signUpRequest.getPassword()));
        user.setDateJoined(java.time.OffsetDateTime.now());
        user.setIsActive(true); // Mặc định là Active

        String roleName = signUpRequest.getRole();

        if (roleName == null || roleName.isBlank()) {
            // Trả về lỗi nếu không có vai trò nào được chỉ định
            return ResponseEntity.badRequest().body("Lỗi: Phải chỉ định một vai trò (Role) duy nhất cho tài khoản.");
        }

        // Xử lý và tìm kiếm Roles
        try {
            // 1. Chuyển chuỗi thành RoleType Enum
            // (Ví dụ: "BRAND_MANAGER" -> RoleType.BRAND_MANAGER)
            RoleType roleType = RoleType.valueOf(roleName.toUpperCase());

            // 2. Tìm kiếm Entity Role dựa trên RoleType
            Role roleEntity = roleRepository.findByRoleType(roleType)
                    .orElseThrow(() -> new RuntimeException("Lỗi: Role " + roleName + " không tìm thấy trong DB."));

            // 3. Ngăn không cho Admin tự tạo Admin khác qua API này (tùy chọn bảo mật)
            if (roleType == RoleType.ADMIN && !SecurityContextHolder.getContext().getAuthentication().getName().equals(signUpRequest.getUsername())) {
                // Nếu người gọi API không phải là người đang được tạo (tránh việc tạo Admin mới qua API)
                // Bạn có thể chọn BỎ QUA hoặc BÁO LỖI. Ở đây ta BÁO LỖI để Admin phải cẩn thận
                return ResponseEntity.badRequest().body("Lỗi: Không được phân quyền ADMIN qua API này.");
            }

            user.setRole(roleEntity);

        } catch (IllegalArgumentException ex) {
            // Xử lý khi chuỗi roleName không khớp với bất kỳ RoleType Enum nào
            return ResponseEntity.badRequest().body("Lỗi: Vai trò '" + roleName + "' không hợp lệ.");
        }
        userRepository.save(user);
        return ResponseEntity.ok("Đăng ký tài khoản thành công!");
    }
}