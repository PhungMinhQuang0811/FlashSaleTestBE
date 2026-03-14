package com.mp.flashsale.mapper;

import com.mp.flashsale.dto.request.user.AccountRegisterRequest;
import com.mp.flashsale.dto.response.user.UserResponse;
import com.mp.flashsale.entity.Account;
import org.mapstruct.*;


@Mapper(componentModel = "spring")
public interface UserMapper {
    /**
     * Converts an {@code AccountRegisterRequest} to an {@code Account} entity.
     *
     * @param request the account registration request containing user input data
     * @return an {@code Account} entity constructed from the provided request data
     */
    Account toAccount(AccountRegisterRequest request);
    @Mapping(target = "role", source = "account.role.roleName")
    UserResponse toUserResponse(Account account);
}
