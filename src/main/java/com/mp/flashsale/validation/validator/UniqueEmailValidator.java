package com.mp.flashsale.validation.validator;


import com.mp.flashsale.repository.AccountRepository;
import com.mp.flashsale.validation.UniqueEmail;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

    private final AccountRepository accountRepository;


    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return accountRepository.findByEmail(s).isEmpty();
    }
}
