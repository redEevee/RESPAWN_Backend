package com.shop.respawn.repository;

import com.shop.respawn.domain.Role;
import com.shop.respawn.dto.query.UserQueryDto;
import com.shop.respawn.dto.query.FailureResultDto;

import java.time.LocalDateTime;
import java.util.Optional;

public interface BuyerRepositoryCustom {
    UserQueryDto findUserDtoByUsername(String username);

    Optional<LocalDateTime> findLastPasswordChangedAtByUsername(String username);

    long resetFailedLoginByUsername(String username);

    FailureResultDto increaseFailedAttemptsAndGetStatus(String username);

    Role findUserDtoRoleByUsername(String username);
}
