package cnstn.system_de_reservation_cnstn.services;

import cnstn.system_de_reservation_cnstn.models.Equipement;
import cnstn.system_de_reservation_cnstn.repository.EquipementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipementService {
    private final EquipementRepository equipementRepository;

    public Equipement createEquipement(Equipement equipement) {
        if (equipement.getDateAquisation() == null) {
            equipement.setDateAquisation(new java.util.Date());
        }
        if (equipement.getEtat() == null || equipement.getEtat().isBlank()) {
            equipement.setEtat("Bon");
        }
        if (equipement.getReservable() == null) {
            equipement.setReservable(true);
        }
        return equipementRepository.save(equipement);

    }

    public List<Equipement> findAll() {
        return equipementRepository.findAll();
    }

    public Equipement updateEquipement(Long id, Equipement equipement) {
        Equipement existingEquipement = equipementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipement not found with id: " + id));
        if (equipement.getNom() != null) {
            existingEquipement.setNom(equipement.getNom());
        }
        if (equipement.getNumeroSerie() != null) {
            existingEquipement.setNumeroSerie(equipement.getNumeroSerie());
        }
        if (equipement.getEtat() != null) {
            existingEquipement.setEtat(equipement.getEtat());
        }
        if (equipement.getReservable() != null) {
            existingEquipement.setReservable(equipement.getReservable());
        }
        if (equipement.getTypeEquipement() != null) {
            existingEquipement.setTypeEquipement(equipement.getTypeEquipement());
        }
        return equipementRepository.save(existingEquipement);
    }

    public void deleteById(Long id) {
        equipementRepository.deleteById(id);
    }
}
