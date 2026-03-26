package cnstn.system_de_reservation_cnstn.repository;

import cnstn.system_de_reservation_cnstn.models.PendingRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PendingRegistrationRepository extends JpaRepository<PendingRegistration, Long> {
    boolean existsByEmail(String email);
    boolean existsByEmailAndEmailVerifiedTrue(String email);
    Optional<PendingRegistration> findByEmail(String email);
    Optional<PendingRegistration> findByVerificationCode(String verificationCode);
}
