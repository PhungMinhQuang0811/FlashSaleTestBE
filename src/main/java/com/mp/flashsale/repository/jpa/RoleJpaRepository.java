package com.mp.flashsale.repository.jpa;

import com.mp.flashsale.constant.ERoleName;
import com.mp.flashsale.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleJpaRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleName(ERoleName name);
}
