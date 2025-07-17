package com.shop.respawn.sms.Verification;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class VerificationCodeRepository {
    private final Map<String, VerificationCode> repository = new ConcurrentHashMap<>();

    public void save(VerificationCode verificationCode) {
        repository.put(verificationCode.getCode(), verificationCode);
    }

    public Optional<VerificationCode> findByCode(String code) {
        return Optional.ofNullable(repository.get(code));
    }

    public void remove(VerificationCode verificationCode) {
        repository.remove(verificationCode.getCode());
    }
}
