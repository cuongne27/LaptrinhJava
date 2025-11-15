package com.evm.backend.repository;

import com.evm.backend.entity.Role;
import com.evm.backend.entity.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleName(String roleName);

    Optional<Role> findByRoleType(RoleType roleType);

    List<Role> findByRoleTypeIn(List<RoleType> roleTypes);

    boolean existsByRoleName(String roleName);

    Optional<Role> findById(Long id);
}