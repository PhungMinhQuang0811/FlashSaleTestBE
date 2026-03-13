package com.mp.flashsale.repository;

import com.mp.flashsale.constant.ERoleName;
import com.mp.flashsale.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


public interface RoleRepository  {
    Optional<Role> findByName(ERoleName name);
    Role save(Role role);
}
