package com.evm.backend.repository;

import com.evm.backend.entity.Brand;
import com.evm.backend.entity.Dealer;
import com.evm.backend.entity.Role;
import com.evm.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findByRole(Role role);

    List<User> findByRoleId(Integer roleId);

    List<User> findByDealer(Dealer dealer);

    List<User> findByDealerId(Long dealerId);

    List<User> findByBrand(Brand brand);

    List<User> findByBrandId(Integer brandId);

    List<User> findByIsActive(Boolean isActive);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.dealer.id = :dealerId AND u.role.roleType = :roleType")
    List<User> findByDealerAndRoleType(@Param("dealerId") Long dealerId,
                                       @Param("roleType") com.evm.backend.entity.RoleType roleType);

    @Query("SELECT u FROM User u WHERE u.fullName LIKE %:keyword% OR u.username LIKE %:keyword%")
    List<User> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.role " +
            "LEFT JOIN FETCH u.dealer " +
            "LEFT JOIN FETCH u.brand " +
            "WHERE u.username = :username")
    Optional<User> findByUsernameWithDetails(@Param("username") String username);

    long countByDealerId(Long dealerId);

    long countByIsActive(Boolean isActive);
}