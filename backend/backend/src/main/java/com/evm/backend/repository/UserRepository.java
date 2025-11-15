package com.evm.backend.repository;

import com.evm.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity
 * Bạn có thể thêm các methods này vào UserRepository hiện tại
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    // ===== Methods for Authentication (đã có) =====
    Optional<User> findByUsername(String username);

    // ===== Additional Methods for CRUD =====

    /**
     * Find user by ID with all details
     */
    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.role r " +
            "LEFT JOIN FETCH u.brand b " +
            "LEFT JOIN FETCH u.dealer d " +
            "WHERE u.id = :userId")
    Optional<User> findByIdWithDetails(@Param("userId") Long userId);

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Find users by role name
     */
    @Query("SELECT u FROM User u " +
            "JOIN u.role r " +
            "WHERE r.roleName = :roleName " +
            "ORDER BY u.fullName ASC")
    List<User> findByRoleName(@Param("roleName") String roleName);

    /**
     * Find users by brand ID
     */
    @Query("SELECT u FROM User u WHERE u.brand.id = :brandId ORDER BY u.fullName ASC")
    List<User> findByBrandId(@Param("brandId") Integer brandId);

    /**
     * Find users by dealer ID
     */
    @Query("SELECT u FROM User u WHERE u.dealer.id = :dealerId ORDER BY u.fullName ASC")
    List<User> findByDealerId(@Param("dealerId") Long dealerId);

    /**
     * Find users by active status
     */
    @Query("SELECT u FROM User u WHERE u.isActive = :isActive ORDER BY u.dateJoined DESC")
    List<User> findByIsActive(@Param("isActive") Boolean isActive);

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Count users by role
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role.roleName = :roleName")
    Long countByRoleName(@Param("roleName") String roleName);

    /**
     * Count active users
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    Long countActiveUsers();

    /**
     * Find users by brand and dealer
     */
    @Query("SELECT u FROM User u " +
            "WHERE u.brand.id = :brandId " +
            "AND u.dealer.id = :dealerId " +
            "ORDER BY u.fullName ASC")
    List<User> findByBrandIdAndDealerId(
            @Param("brandId") Integer brandId,
            @Param("dealerId") Long dealerId
    );

    /**
     * Find users by role and active status
     */
    @Query("SELECT u FROM User u " +
            "JOIN u.role r " +
            "WHERE r.roleName = :roleName " +
            "AND u.isActive = :isActive " +
            "ORDER BY u.fullName ASC")
    List<User> findByRoleNameAndIsActive(
            @Param("roleName") String roleName,
            @Param("isActive") Boolean isActive
    );
}