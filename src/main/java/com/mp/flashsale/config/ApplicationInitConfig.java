package com.mp.flashsale.config;

import com.mp.flashsale.constant.ERoleName;
import com.mp.flashsale.entity.Account;
import com.mp.flashsale.entity.Role;
import com.mp.flashsale.repository.AccountRepository;
import com.mp.flashsale.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;


@Configuration
@RequiredArgsConstructor()
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    RoleRepository roleRepository;
    AccountRepository accountRepository;
    PasswordEncoder passwordEncoder;

    @Value("${flashsale.init.enabled:false}")
    @NonFinal
    boolean isInitEnabled;

    @Value("${flashsale.init.admin.email}")
    @NonFinal
    String adminEmail;

    @Value("${flashsale.init.admin.password}")
    @NonFinal
    String adminPassword;

    @Bean
    ApplicationRunner init() {
        return args -> {
            if (!isInitEnabled) {
                log.info("Skip seeding data (enabled=false)");
                return;
            }
            createRoleIfNotFound(ERoleName.ADMIN);
            createRoleIfNotFound(ERoleName.CUSTOMER);
            createRoleIfNotFound(ERoleName.SELLER);

            // 2. Seed Admin Account
            if (accountRepository.findByEmail(adminEmail).isEmpty()) {
                Role adminRole = roleRepository.findByName(ERoleName.ADMIN)
                        .orElseThrow(() -> new RuntimeException("Role Admin not found"));

                Account adminAccount = new Account();
                adminAccount.setEmail(adminEmail);
                adminAccount.setPassword(passwordEncoder.encode(adminPassword));
                adminAccount.setRole(adminRole);

                adminAccount.setActive(true);
                adminAccount.setEmailVerified(true);

                accountRepository.save(adminAccount);
                log.info("Seed data success. Admin account was created");
            }
        };
    }


    private void createRoleIfNotFound(ERoleName name) {
        if (roleRepository.findByName(name).isEmpty()) {
            roleRepository.save(Role.builder().roleName(name).build());
            log.info("Create role {}", name);
        }
    }
}
