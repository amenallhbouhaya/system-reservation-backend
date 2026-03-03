package cnstn.system_de_reservation_cnstn.repository;

import cnstn.system_de_reservation_cnstn.models.Salle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaleRepository extends JpaRepository<Salle, Long> {

    List<Salle> findByEvenementIsNull();

    List<Salle> findByEvenementId(Long evenementId);
}