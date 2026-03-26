package cnstn.system_de_reservation_cnstn.repository;

import cnstn.system_de_reservation_cnstn.models.Intervention;
import cnstn.system_de_reservation_cnstn.models.InterventionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface InterventionRepository extends JpaRepository<Intervention, Long> {
    List<Intervention> findByUtilisateurEmail(String email);
    long countByStatut(InterventionStatus statut);
    List<Intervention> findByStatut(InterventionStatus statut);
    List<Intervention> findByStatutIn(Set<InterventionStatus> statuts);
    List<Intervention> findByEquipementIdAndStatutIn(Long equipementId, Set<InterventionStatus> statuts);
    List<Intervention> findByEquipementId(Long equipementId);
}