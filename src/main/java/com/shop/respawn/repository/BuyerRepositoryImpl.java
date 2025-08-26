package com.shop.respawn.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shop.respawn.domain.Role;
import com.shop.respawn.dto.query.UserQueryDto;
import com.shop.respawn.dto.query.FailureResultDto;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.shop.respawn.domain.QBuyer.buyer;

@Repository
public class BuyerRepositoryImpl implements BuyerRepositoryCustom {

    private final BaseRepositoryImpl base; // 생성자 주입 권장

    // 생성자에서 주입 받는 형태 권장
    public BuyerRepositoryImpl(JPAQueryFactory queryFactory) {
        this.base = new BaseRepositoryImpl(queryFactory);
    }

    @Override
    public Role findUserDtoRoleByUsername(String username) {
        return base.findUserDtoRoleByUsername(buyer, buyer.role, buyer.username, username);
    }

    @Override
    public UserQueryDto findUserDtoByUsername(String username) {
        return base.findUserDtoByUsername(
                buyer, buyer.id, buyer.name, buyer.role, buyer.username, username
        );
    }

    @Override
    public Optional<LocalDateTime> findLastPasswordChangedAtByUsername(String username) {
        return base.findLastPasswordChangedAt(
                buyer, buyer.username, buyer.accountStatus.lastPasswordChangedAt, username
        );
    }

    @Override
    public long resetFailedLoginByUsername(String username) {
        return base.resetFailedLogin(
                buyer, buyer.username, buyer.accountStatus.accountNonLocked, buyer.accountStatus.failedLoginAttempts, username
        );
    }

    @Override
    public FailureResultDto increaseFailedAttemptsAndGetStatus(String username) {
        return base.increaseFailedAttemptsAndGetStatus(
                buyer,
                buyer.username,
                buyer.accountStatus.enabled,
                buyer.accountStatus.accountExpiryDate,
                buyer.accountStatus.accountNonLocked,
                buyer.accountStatus.failedLoginAttempts,
                username,
                5
        );
    }

    @Override
    public boolean existsUserIdentityConflict(String email, String phoneNumber, String username) {
        return base.existsUserIdentityConflict(
                buyer,
                buyer.email,
                buyer.phoneNumber,
                buyer.username,
                email,
                phoneNumber,
                username
        );
    }
}
