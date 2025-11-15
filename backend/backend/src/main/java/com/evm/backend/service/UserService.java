package com.evm.backend.service;

import com.evm.backend.dto.request.UserFilterRequest;
import com.evm.backend.dto.request.UserRequest;
import com.evm.backend.dto.response.UserDetailResponse;
import com.evm.backend.dto.response.UserListResponse;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Service interface for User management (Staff/Employee CRUD)
 * Only ADMIN can perform CRUD operations
 */
public interface UserService {

    /**
     * Get all users with filtering and pagination
     */
    Page<UserListResponse> getAllUsers(UserFilterRequest filterRequest);

    /**
     * Get users by role
     */
    List<UserListResponse> getUsersByRole(String roleName);

    /**
     * Get users by brand
     */
    List<UserListResponse> getUsersByBrand(Integer brandId);

    /**
     * Get users by dealer
     */
    List<UserListResponse> getUsersByDealer(Long dealerId);

    /**
     * Get active users
     */
    List<UserListResponse> getActiveUsers();

    /**
     * Get inactive users
     */
    List<UserListResponse> getInactiveUsers();

    /**
     * Get user detail by ID
     */
    UserDetailResponse getUserById(Long userId);

    /**
     * Get user by username
     */
    UserDetailResponse getUserByUsername(String username);

    /**
     * Get user by email
     */
    UserDetailResponse getUserByEmail(String email);

    /**
     * Create new user (Admin creates account for staff)
     */
    UserDetailResponse createUser(UserRequest request);

    /**
     * Update user
     */
    UserDetailResponse updateUser(Long userId, UserRequest request);

    /**
     * Activate user account
     */
    UserDetailResponse activateUser(Long userId);

    /**
     * Deactivate user account
     */
    UserDetailResponse deactivateUser(Long userId);

    /**
     * Reset user password (Admin resets for staff)
     */
    UserDetailResponse resetPassword(Long userId, String newPassword);

    /**
     * Delete user (soft delete - set isActive = false)
     */
    void deleteUser(Long userId);

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
}