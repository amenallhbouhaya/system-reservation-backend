package cnstn.system_de_reservation_cnstn.repository;

import cnstn.system_de_reservation_cnstn.models.EvenementInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EvenementInvitationRepository extends JpaRepository<EvenementInvitation, Long> {
    Optional<EvenementInvitation> findByReferenceCode(String referenceCode);
    Optional<EvenementInvitation> findByEvenementIdAndUtilisateurId(Long evenementId, Long utilisateurId);
    boolean existsByReferenceCode(String referenceCode);
}
