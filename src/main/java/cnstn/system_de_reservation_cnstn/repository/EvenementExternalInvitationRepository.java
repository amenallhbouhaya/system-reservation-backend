package cnstn.system_de_reservation_cnstn.repository;

import cnstn.system_de_reservation_cnstn.models.EvenementExternalInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EvenementExternalInvitationRepository extends JpaRepository<EvenementExternalInvitation, Long> {
    List<EvenementExternalInvitation> findByEvenementId(Long evenementId);
    Optional<EvenementExternalInvitation> findByReferenceCode(String referenceCode);
}
