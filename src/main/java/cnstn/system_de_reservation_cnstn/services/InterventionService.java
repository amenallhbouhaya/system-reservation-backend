package cnstn.system_de_reservation_cnstn.services;

import cnstn.system_de_reservation_cnstn.dto.CreateInterventionRequest;
import cnstn.system_de_reservation_cnstn.dto.InterventionDto;
import cnstn.system_de_reservation_cnstn.models.Equipement;
import cnstn.system_de_reservation_cnstn.models.Intervention;
import cnstn.system_de_reservation_cnstn.models.Utilisateur;
import cnstn.system_de_reservation_cnstn.repository.EquipementRepository;
import cnstn.system_de_reservation_cnstn.repository.InterventionRepository;
import cnstn.system_de_reservation_cnstn.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InterventionService {

    private final InterventionRepository interventionRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EquipementRepository equipementRepository;

    public InterventionDto create(String email, CreateInterventionRequest req) {
        Utilisateur u = utilisateurRepository.findByEmail(email).orElseThrow();

        Intervention i = new Intervention();
        i.setDescription(req.description());
        i.setStatut("EN_ATTENTE");
        i.setDateDemande(new Date());
        i.setUtilisateur(u);

        if (req.equipementIds() != null && !req.equipementIds().isEmpty()) {
            List<Equipement> eqs = equipementRepository.findAllById(req.equipementIds());
            i.setEquipement(eqs);
        }

        Intervention saved = interventionRepository.save(i);
        return toDto(saved);
    }

    public List<InterventionDto> myInterventions(String email) {
        return interventionRepository.findByUtilisateurEmail(email)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private InterventionDto toDto(Intervention i) {
        List<Long> eqIds = (i.getEquipement() == null) ? List.of()
                : i.getEquipement().stream().map(Equipement::getId).collect(Collectors.toList());

        return new InterventionDto(
                i.getId(),
                i.getDescription(),
                i.getStatut(),
                i.getDateDemande(),
                i.getUtilisateur() != null ? i.getUtilisateur().getId() : null,
                i.getUtilisateur() != null ? i.getUtilisateur().getEmail() : null,
                eqIds
        );
    }
}