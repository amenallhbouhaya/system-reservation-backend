package cnstn.system_de_reservation_cnstn.repository;

import cnstn.system_de_reservation_cnstn.models.Equipement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface EquipementRepository extends JpaRepository<Equipement , Long> {
    Optional<Equipement >findEquipementById(Long id);
    List<Equipement> findByEvenementId(Long evenementId);
    List<Equipement> findByEvenementIsNullAndReservableTrue();
}
