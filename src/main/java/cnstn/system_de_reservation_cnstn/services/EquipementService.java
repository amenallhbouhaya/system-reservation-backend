package cnstn.system_de_reservation_cnstn.services;

import cnstn.system_de_reservation_cnstn.dto.equipement.EquipementRequest;
import cnstn.system_de_reservation_cnstn.dto.equipement.EquipementResponse;
import cnstn.system_de_reservation_cnstn.models.Equipement;
import cnstn.system_de_reservation_cnstn.repository.EquipementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipementService {
    private final EquipementRepository equipementRepository;

    public EquipementResponse create(EquipementRequest request) {
        Equipement equipement = new Equipement();
        applyCreateRequest(equipement, request);
        return toResponse(equipementRepository.save(equipement));
    }

    public List<EquipementResponse> findAllResponses() {
        return equipementRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public EquipementResponse update(Long id, EquipementRequest request) {
        Equipement existingEquipement = equipementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipement not found with id: " + id));
        applyUpdateRequest(existingEquipement, request);
        return toResponse(equipementRepository.save(existingEquipement));
    }

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

    private void applyCreateRequest(Equipement equipement, EquipementRequest request) {
        equipement.setDateAquisation(request.dateAquisation() != null ? request.dateAquisation() : new java.util.Date());
        equipement.setNom(request.nom());
        equipement.setEtat((request.etat() == null || request.etat().isBlank()) ? "Bon" : request.etat());
        equipement.setReservable(request.reservable() != null ? request.reservable() : true);
        equipement.setNumeroSerie(request.numeroSerie());
        equipement.setTypeEquipement(request.typeEquipement());
    }

    private void applyUpdateRequest(Equipement equipement, EquipementRequest request) {
        if (request.dateAquisation() != null) {
            equipement.setDateAquisation(request.dateAquisation());
        }
        if (request.nom() != null) {
            equipement.setNom(request.nom());
        }
        if (request.numeroSerie() != null) {
            equipement.setNumeroSerie(request.numeroSerie());
        }
        if (request.etat() != null) {
            equipement.setEtat(request.etat());
        }
        if (request.reservable() != null) {
            equipement.setReservable(request.reservable());
        }
        if (request.typeEquipement() != null) {
            equipement.setTypeEquipement(request.typeEquipement());
        }
    }

    private EquipementResponse toResponse(Equipement equipement) {
        return new EquipementResponse(
                equipement.getId(),
                equipement.getDateAquisation(),
                equipement.getNom(),
                equipement.getEtat(),
                equipement.getReservable(),
                equipement.getNumeroSerie(),
                equipement.getTypeEquipement()
        );
    }
}
