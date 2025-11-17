package com.evm.backend.config;

import java.time.OffsetDateTime;
import com.evm.backend.entity.Role;
import com.evm.backend.entity.RoleType;
import com.evm.backend.entity.User;
import com.evm.backend.repository.RoleRepository;
import com.evm.backend.repository.UserRepository;
import com.evm.backend.service.UserDetailsServiceImpl;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EnableMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
)
public class SecurityConfiguration {
    UserDetailsServiceImpl userDetailsService;
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Cấu hình CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Tắt CSRF vì dùng JWT token và stateless
                .csrf(AbstractHttpConfigurer::disable)
                // Cấu hình session là stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Cấu hình phân quyền
                .authorizeHttpRequests(auth ->
                                auth
                                        // Cho phép đăng nhập public
                                        .requestMatchers("/api/auth/login").permitAll()
                                        .requestMatchers(
                                                "/swagger-ui/**",
                                                "/swagger-ui.html",
                                                "/v3/api-docs/**",
                                                "/swagger-resources/**",
                                                "/webjars/**"
                                        ).permitAll()

                                        // CHỈ ADMIN MỚI ĐƯỢC DÙNG CHỨC NĂNG ĐĂNG KÝ
                                        .requestMatchers("/api/auth/sign-up").hasRole("ADMIN")

                                        // ... (các đường dẫn khác)
                                        .anyRequest().authenticated()

                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);
        // Thêm JWT filter trước UsernamePasswordAuthenticationFilter
        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public CommandLineRunner initRolesAndAdmin(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            // CHECK ROLE TỒN TẠI HAY CHƯA
            if (roleRepository.count() == 0) {
                List<Role> roles = Arrays.asList(
                        // Admin Role
                        Role.builder()
                                .roleType(RoleType.ADMIN)
                                .roleName("ADMIN")
                                .displayName("Quản trị viên")
                                .description("Quản trị hệ thống, toàn quyền")
                                .build(),

                        // Brand Manager Role
                        Role.builder()
                                .roleType(RoleType.BRAND_MANAGER)
                                .roleName("BRAND_MANAGER")
                                .displayName("Quản lý thương hiệu")
                                .description("Quản lý thương hiệu, sản phẩm, phân phối")
                                .build(),

                        // Dealer Manager Role
                        Role.builder()
                                .roleType(RoleType.DEALER_MANAGER)
                                .roleName("DEALER_MANAGER")
                                .displayName("Quản lý đại lý")
                                .description("Quản lý showroom, nhân viên, kho")
                                .build(),

                        // Sales Person Role
                        Role.builder()
                                .roleType(RoleType.SALES_PERSON)
                                .roleName("SALES_PERSON")
                                .displayName("Nhân viên bán hàng")
                                .description("Tư vấn, bán hàng, chăm sóc khách hàng")
                                .build(),

                        // Support Staff Role
                        Role.builder()
                                .roleType(RoleType.SUPPORT_STAFF)
                                .roleName("SUPPORT_STAFF")
                                .displayName("Nhân viên hỗ trợ")
                                .description("Hỗ trợ kỹ thuật, bảo hành, bảo dưỡng")
                                .build(),

                        // Warehouse Staff Role
                        Role.builder()
                                .roleType(RoleType.WAREHOUSE_STAFF)
                                .roleName("WAREHOUSE_STAFF")
                                .displayName("Nhân viên kho")
                                .description("Quản lý kho, nhập xuất xe")
                                .build()
                );

                roleRepository.saveAll(roles);
                System.out.println("Roles initialized successfully");
            }

            // TẠO TÀI KHOẢN ADMIN NẾU CHƯA TỒN TẠI
            if (!userRepository.existsByUsername("admin")) {
                Role adminRole = roleRepository.findByRoleType(RoleType.ADMIN)
                        .orElseThrow(() -> new RuntimeException("KHÔNG TÌM THẤY ROLE"));

                User adminUser = User.builder()
                        .username("admin")
                        .passwordHash(passwordEncoder.encode("admin123"))
                        .fullName("TÔN QUỲNH LONG")
                        .email("longtq9662@ut.edu.vn")
                        .dateJoined(OffsetDateTime.now())
                        .isActive(true)
                        .role(adminRole)
                        .build();

                userRepository.save(adminUser);
                System.out.println("TẠO TÀI KHOẢN ADMIN THÀNH CÔNG");
            }
        };
    }
}
