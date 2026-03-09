package cnstn.system_de_reservation_cnstn.controllers;

import cnstn.system_de_reservation_cnstn.dto.DecisionRequest;
import cnstn.system_de_reservation_cnstn.dto.EvenementPendingDto;
import cnstn.system_de_reservation_cnstn.models.Evenement;
import cnstn.system_de_reservation_cnstn.models.EvenementStatut;
import cnstn.system_de_reservation_cnstn.models.Salle;
import cnstn.system_de_reservation_cnstn.repository.EquipementRepository;
import cnstn.system_de_reservation_cnstn.repository.EvenmentRepository;
import cnstn.system_de_reservation_cnstn.repository.SaleRepository;
import cnstn.system_de_reservation_cnstn.services.EvenementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/directeur-dsn/evenements")
@RequiredArgsConstructor
public class DirecteurDsnEvenementController {

    private final EvenmentRepository evenmentRepository;
    private final EvenementService evenementService;
    private final SaleRepository salleRepository;
    private final EquipementRepository equipementRepository;

    @GetMapping("/pending")
    public List<EvenementPendingDto> pending() {
        return evenmentRepository.findByStatut(EvenementStatut.EN_ATTENTE_DSN)
                .stream()
                .map(e -> {
                    List<Salle> salles = salleRepository.findByEvenementId(e.getId());
                    String salleNom = salles.isEmpty() ? null : salles.get(0).getNom();
                    Integer salleCap = salles.isEmpty() ? null : salles.get(0).getCapacite();

                    List<String> eqNames = equipementRepository.findByEvenementId(e.getId())
                            .stream()
                            .map(eq -> eq.getTypeEquipement() + " - " + eq.getEtat())
                            .toList();

                    String email = (e.getUtilisateur() != null) ? e.getUtilisateur().getEmail() : null;

                    return new EvenementPendingDto(
                            e.getId(),
                            e.getTitre(),
                            e.getDateDebut(),
                            e.getDateFin(),
                            e.getTypeEvenement(),
                            e.getStatut().toString(),
                            salleNom,
                            salleCap,
                            eqNames,
                            email
                    );
                })
                .toList();
    }

    @PutMapping("/{id}/accept")
    public Evenement accept(@PathVariable Long id) {
        return evenementService.dsnAccept(id);
    }

    @PutMapping("/{id}/reject")
    public Evenement reject(@PathVariable Long id, @RequestBody DecisionRequest req) {
        String commentaire = (req == null) ? null : req.commentaire();
        return evenementService.dsnReject(id, commentaire);
    }
}