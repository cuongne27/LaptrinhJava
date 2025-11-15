package com.evm.backend.service.impl;

import com.evm.backend.dto.request.UserFilterRequest;
import com.evm.backend.dto.request.UserRequest;
import com.evm.backend.dto.response.UserDetailResponse;
import com.evm.backend.dto.response.UserListResponse;
import com.evm.backend.entity.*;
import com.evm.backend.exception.BadRequestException;
import com.evm.backend.exception.ResourceNotFoundException;
import com.evm.backend.repository.*;
import com.evm.backend.service.UserService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BrandRepository brandRepository;
    private final DealerRepository dealerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public Page<UserListResponse> getAllUsers(UserFilterRequest filterRequest) {
        log.info("Getting all users with filter: {}", filterRequest);

        Specification<User> spec = buildSpecification(filterRequest);
        Pageable pageable = buildPageable(filterRequest);

        Page<User> users = userRepository.findAll(spec, pageable);

        return users.map(this::convertToListResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserListResponse> getUsersByRole(String roleName) {
        log.info("Getting users by role: {}", roleName);

        List<User> users = userRepository.findByRoleName(roleName);

        return users.stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserListResponse> getUsersByBrand(Integer brandId) {
        log.info("Getting users by brand: {}", brandId);

        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found: " + brandId));

        List<User> users = userRepository.findByBrandId(brandId);

        return users.stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserListResponse> getUsersByDealer(Long dealerId) {
        log.info("Getting users by dealer: {}", dealerId);

        Dealer dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found: " + dealerId));

        List<User> users = userRepository.findByDealerId(dealerId);

        return users.stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserListResponse> getActiveUsers() {
        log.info("Getting active users");

        List<User> users = userRepository.findByIsActive(true);

        return users.stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserListResponse> getInactiveUsers() {
        log.info("Getting inactive users");

        List<User> users = userRepository.findByIsActive(false);

        return users.stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailResponse getUserById(Long userId) {
        log.info("Getting user by id: {}", userId);

        User user = userRepository.findByIdWithDetails(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        return convertToDetailResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailResponse getUserByUsername(String username) {
        log.info("Getting user by username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        return convertToDetailResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailResponse getUserByEmail(String email) {
        log.info("Getting user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return convertToDetailResponse(user);
    }

    @Override
    public UserDetailResponse createUser(UserRequest request) {
        log.info("Creating user: {}", request.getUsername());

        // Validate username uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists: " + request.getUsername());
        }

        // Validate email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists: " + request.getEmail());
        }

        // Get role
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.getRoleId()));

        // Get brand (if specified)
        Brand brand = null;
        if (request.getBrandId() != null) {
            brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand not found: " + request.getBrandId()));
        }

        // Get dealer (if specified)
        Dealer dealer = null;
        if (request.getDealerId() != null) {
            dealer = dealerRepository.findById(request.getDealerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Dealer not found: " + request.getDealerId()));
        }

        // Create user
        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .role(role)
                .brand(brand)
                .dealer(dealer)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .dateJoined(OffsetDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        log.info("User created successfully: {}", savedUser.getId());

        return convertToDetailResponse(savedUser);
    }

    @Override
    public UserDetailResponse updateUser(Long userId, UserRequest request) {
        log.info("Updating user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // Check username uniqueness (if changed)
        if (!user.getUsername().equals(request.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new BadRequestException("Username already exists: " + request.getUsername());
            }
            user.setUsername(request.getUsername());
        }

        // Check email uniqueness (if changed)
        if (!user.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email already exists: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        // Update role
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.getRoleId()));
        user.setRole(role);

        // Update brand
        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand not found: " + request.getBrandId()));
            user.setBrand(brand);
        } else {
            user.setBrand(null);
        }

        // Update dealer
        if (request.getDealerId() != null) {
            Dealer dealer = dealerRepository.findById(request.getDealerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Dealer not found: " + request.getDealerId()));
            user.setDealer(dealer);
        } else {
            user.setDealer(null);
        }

        // Update other fields
        user.setFullName(request.getFullName());
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }

        // Note: Password is NOT updated here, use resetPassword instead

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", userId);

        return convertToDetailResponse(updatedUser);
    }

    @Override
    public UserDetailResponse activateUser(Long userId) {
        log.info("Activating user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (Boolean.TRUE.equals(user.getIsActive())) {
            throw new BadRequestException("User is already active");
        }

        user.setIsActive(true);
        User activatedUser = userRepository.save(user);
        log.info("User activated successfully: {}", userId);

        return convertToDetailResponse(activatedUser);
    }

    @Override
    public UserDetailResponse deactivateUser(Long userId) {
        log.info("Deactivating user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new BadRequestException("User is already inactive");
        }

        user.setIsActive(false);
        User deactivatedUser = userRepository.save(user);
        log.info("User deactivated successfully: {}", userId);

        return convertToDetailResponse(deactivatedUser);
    }

    @Override
    public UserDetailResponse resetPassword(Long userId, String newPassword) {
        log.info("Resetting password for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (newPassword == null || newPassword.length() < 6) {
            throw new BadRequestException("Password must be at least 6 characters");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        User updatedUser = userRepository.save(user);
        log.info("Password reset successfully for user: {}", userId);

        return convertToDetailResponse(updatedUser);
    }

    @Override
    public void deleteUser(Long userId) {
        log.info("Deleting user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // Soft delete - set isActive = false
        user.setIsActive(false);
        userRepository.save(user);
        log.info("User soft deleted successfully: {}", userId);

        // If you want hard delete, use:
        // userRepository.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // ===== HELPER METHODS =====

    private Specification<User> buildSpecification(UserFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Search keyword in username, fullName, email
            if (filter.getSearchKeyword() != null && !filter.getSearchKeyword().isEmpty()) {
                String keyword = "%" + filter.getSearchKeyword().toLowerCase() + "%";
                Predicate usernamePred = cb.like(cb.lower(root.get("username")), keyword);
                Predicate fullNamePred = cb.like(cb.lower(root.get("fullName")), keyword);
                Predicate emailPred = cb.like(cb.lower(root.get("email")), keyword);
                predicates.add(cb.or(usernamePred, fullNamePred, emailPred));
            }

            if (filter.getRoleName() != null && !filter.getRoleName().isEmpty()) {
                predicates.add(cb.equal(root.get("role").get("roleName"), filter.getRoleName()));
            }

            if (filter.getRoleId() != null) {
                predicates.add(cb.equal(root.get("role").get("id"), filter.getRoleId()));
            }

            if (filter.getBrandId() != null) {
                predicates.add(cb.equal(root.get("brand").get("id"), filter.getBrandId()));
            }

            if (filter.getDealerId() != null) {
                predicates.add(cb.equal(root.get("dealer").get("id"), filter.getDealerId()));
            }

            if (filter.getIsActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), filter.getIsActive()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Pageable buildPageable(UserFilterRequest filter) {
        int page = filter.getPage() != null ? filter.getPage() : 0;
        int size = filter.getSize() != null ? Math.min(filter.getSize(), 100) : 20;

        Sort sort = Sort.by(Sort.Direction.DESC, "dateJoined");

        if (filter.getSortBy() != null) {
            switch (filter.getSortBy()) {
                case "username_asc":
                    sort = Sort.by(Sort.Direction.ASC, "username");
                    break;
                case "username_desc":
                    sort = Sort.by(Sort.Direction.DESC, "username");
                    break;
                case "fullname_asc":
                    sort = Sort.by(Sort.Direction.ASC, "fullName");
                    break;
                case "fullname_desc":
                    sort = Sort.by(Sort.Direction.DESC, "fullName");
                    break;
                case "created_asc":
                    sort = Sort.by(Sort.Direction.ASC, "dateJoined");
                    break;
                case "created_desc":
                    sort = Sort.by(Sort.Direction.DESC, "dateJoined");
                    break;
                default:
                    break;
            }
        }

        return PageRequest.of(page, size, sort);
    }

    private UserListResponse convertToListResponse(User u) {
        return UserListResponse.builder()
                .userId(u.getId())
                .username(u.getUsername())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .isActive(u.getIsActive())
                .dateJoined(u.getDateJoined())
                // Role info
                .roleId(Long.valueOf(u.getRole() != null ? u.getRole().getId() : null))
                .roleName(u.getRole() != null ? u.getRole().getRoleName() : null)
                .roleDisplayName(u.getRole() != null ? u.getRole().getDisplayName() : null)
                // Brand info
                .brandId(u.getBrand() != null ? u.getBrand().getId() : null)
                .brandName(u.getBrand() != null ? u.getBrand().getBrandName() : null)
                // Dealer info
                .dealerId(u.getDealer() != null ? u.getDealer().getId() : null)
                .dealerName(u.getDealer() != null ? u.getDealer().getDealerName() : null)
                .build();
    }

    private UserDetailResponse convertToDetailResponse(User u) {
        // Count statistics
        Integer totalAppointments = u.getAppointments() != null ? u.getAppointments().size() : 0;
        Integer totalDistributionOrders = u.getDistributionOrders() != null ? u.getDistributionOrders().size() : 0;

        return UserDetailResponse.builder()
                .userId(u.getId())
                .username(u.getUsername())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .isActive(u.getIsActive())
                .dateJoined(u.getDateJoined())
                // Role info
                .roleId(Long.valueOf(u.getRole() != null ? u.getRole().getId() : null))
                .roleName(u.getRole() != null ? u.getRole().getRoleName() : null)
                .roleDisplayName(u.getRole() != null ? u.getRole().getDisplayName() : null)
                .roleType(u.getRole() != null ? String.valueOf(u.getRole().getRoleType()) : null)
                .roleDescription(u.getRole() != null ? u.getRole().getDescription() : null)
                // Brand info
                .brandId(u.getBrand() != null ? u.getBrand().getId() : null)
                .brandName(u.getBrand() != null ? u.getBrand().getBrandName() : null)
                .brandContactInfo(u.getBrand() != null ? u.getBrand().getContactInfo() : null)
                // Dealer info
                .dealerId(u.getDealer() != null ? u.getDealer().getId() : null)
                .dealerName(u.getDealer() != null ? u.getDealer().getDealerName() : null)
                .dealerAddress(u.getDealer() != null ? u.getDealer().getAddress() : null)
                .dealerPhone(u.getDealer() != null ? u.getDealer().getPhoneNumber() : null)
                .dealerEmail(u.getDealer() != null ? u.getDealer().getEmail() : null)
                .dealerLevel(u.getDealer() != null ? u.getDealer().getDealerLevel() : null)
                // Statistics
                .totalAppointments(totalAppointments)
                .totalDistributionOrders(totalDistributionOrders)
                .build();
    }
}