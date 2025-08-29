package com.shop.respawn.repository;

import com.shop.respawn.domain.Role;
import com.shop.respawn.dto.query.UserQueryDto;

public interface AdminRepositoryCustom {
    UserQueryDto findUserDtoByUsername(String username);

    Role findUserDtoRoleByUsername(String username);
}
