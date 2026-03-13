package com.mp.flashsale.repository.impl;

import com.mp.flashsale.constant.ERoleName;
import com.mp.flashsale.entity.Role;
import com.mp.flashsale.repository.RoleRepository;
import com.mp.flashsale.repository.jpa.RoleJpaRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleRepositoryImpl implements RoleRepository {
    RoleJpaRepository roleJpaRepository;
    @Override
    public Optional<Role> findByName(ERoleName name) {
        return roleJpaRepository.findByRoleName(name);
    }

    @Override
    public Role save(Role role) {
        return roleJpaRepository.save(role);
    }
}
