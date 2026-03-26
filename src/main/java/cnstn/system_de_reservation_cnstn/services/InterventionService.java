package cnstn.system_de_reservation_cnstn.services;

import cnstn.system_de_reservation_cnstn.dto.CreateInterventionRequest;
import cnstn.system_de_reservation_cnstn.dto.DsnCompleteInterventionRequest;
import cnstn.system_de_reservation_cnstn.dto.DsnStartInterventionRequest;
import cnstn.system_de_reservation_cnstn.dto.InterventionDto;
import cnstn.system_de_reservation_cnstn.models.Equipement;
import cnstn.system_de_reservation_cnstn.models.Intervention;
import cnstn.system_de_reservation_cnstn.models.InterventionStatus;
import cnstn.system_de_reservation_cnstn.models.InterventionRepairMode;
import cnstn.system_de_reservation_cnstn.models.StockEnPanne;
import cnstn.system_de_reservation_cnstn.models.Utilisateur;
import cnstn.system_de_reservation_cnstn.repository.EquipementRepository;
import cnstn.system_de_reservation_cnstn.repository.InterventionRepository;
import cnstn.system_de_reservation_cnstn.repository.StockEnPanneRepository;
import cnstn.system_de_reservation_cnstn.repository.UtilisateurRepository;
import cnstn.system_de_reservation_cnstn.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class InterventionService {

    private final InterventionRepository interventionRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EquipementRepository equipementRepository;
    private final NotificationService notificationService;
    private final StockEnPanneRepository stockEnPanneRepository;

    public InterventionDto create(String email, CreateInterventionRequest req) {
        Utilisateur u = utilisateurRepository.findByEmail(email).orElseThrow();

        Intervention i = new Intervention();
        i.setNomDemandeur(req.nom());
        i.setDescriptionPanne(req.descriptionPanne());
        i.setTypeAppareil(req.typeAppareil());
        i.setNumeroSerie(req.numeroSerie());
        i.setStatut(InterventionStatus.EN_ATTENTE_CHEF);
        i.setDateDemande(new Date());
        i.setUtilisateur(u);

        if (req.equipementIds() != null && !req.equipementIds().isEmpty()) {
            List<Equipement> eqs = equipementRepository.findAllById(req.equipementIds());
            i.setEquipement(eqs);

            Equipement first = eqs.stream().findFirst().orElse(null);
            if (first != null) {
                if (i.getTypeAppareil() == null || i.getTypeAppareil().isBlank()) {
                    i.setTypeAppareil(String.valueOf(first.getTypeEquipement()));
                }
                if (i.getNumeroSerie() == null || i.getNumeroSerie().isBlank()) {
                    i.setNumeroSerie(first.getNumeroSerie());
                }
            }
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

    public List<InterventionDto> pendingChef() {
        return interventionRepository.findByStatut(InterventionStatus.EN_ATTENTE_CHEF)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<InterventionDto> pendingDsn() {
        return interventionRepository.findByStatutIn(
                Set.of(InterventionStatus.EN_ATTENTE_DSN, InterventionStatus.EN_COURS)
        ).stream().map(this::toDto).collect(Collectors.toList());
    }

    public InterventionDto acceptChef(Long id, String commentaire) {
        Intervention i = interventionRepository.findById(id).orElseThrow();
        if (i.getStatut() != InterventionStatus.EN_ATTENTE_CHEF) {
            throw new RuntimeException("Bad state");
        }

        i.setChefCommentaire(commentaire);
        i.setChefDecisionAt(new Date());
        i.setStatut(InterventionStatus.EN_ATTENTE_DSN);
        return toDto(interventionRepository.save(i));
    }

    public InterventionDto rejectChef(Long id, String commentaire) {
        if (commentaire == null || commentaire.trim().isEmpty()) {
            throw new RuntimeException("Commentaire obligatoire");
        }

        Intervention i = interventionRepository.findById(id).orElseThrow();
        if (i.getStatut() != InterventionStatus.EN_ATTENTE_CHEF) {
            throw new RuntimeException("Bad state");
        }

        i.setChefCommentaire(commentaire.trim());
        i.setChefDecisionAt(new Date());
        i.setStatut(InterventionStatus.REFUSEE_CHEF);
        return toDto(interventionRepository.save(i));
    }

    public InterventionDto startDsn(Long id, DsnStartInterventionRequest req) {
        Intervention i = interventionRepository.findById(id).orElseThrow();
        if (i.getStatut() != InterventionStatus.EN_ATTENTE_DSN) {
            throw new RuntimeException("Bad state");
        }

        InterventionRepairMode mode = req == null ? null : req.repairMode();
        if (mode == null) throw new RuntimeException("Repair mode required");

        i.setRepairMode(mode);
        i.setDsnDecisionAt(new Date());
        i.setStatut(InterventionStatus.EN_COURS);
        Intervention saved = interventionRepository.save(i);
        notificationService.notifyUser(
            saved.getUtilisateur(),
            "Votre intervention est en cours de traitement.",
            "INTERVENTION_STATUS",
            "/employe/interventions"
        );
        return toDto(saved);
    }

    public InterventionDto completeDsn(Long id, DsnCompleteInterventionRequest req) {
        Intervention i = interventionRepository.findById(id).orElseThrow();
        if (i.getStatut() != InterventionStatus.EN_COURS) {
            throw new RuntimeException("Bad state");
        }

        if (req == null || req.dateReparation() == null) {
            throw new RuntimeException("Date reparation required");
        }

        i.setDsnObservation(req.observation());
        i.setDateReparation(req.dateReparation());
        i.setStatut(InterventionStatus.REPARE);
        Intervention saved = interventionRepository.save(i);
        notificationService.notifyUser(
            saved.getUtilisateur(),
            "Votre intervention est reparee. L'equipement est de nouveau disponible.",
            "INTERVENTION_STATUS",
            "/employe/interventions"
        );
        return toDto(saved);
    }

    public InterventionDto repairDsn(Long id) {
        Intervention i = interventionRepository.findById(id).orElseThrow();
        if (i.getStatut() != InterventionStatus.EN_ATTENTE_DSN && i.getStatut() != InterventionStatus.EN_COURS) {
            throw new RuntimeException("Bad state");
        }

        i.setDsnDecisionAt(new Date());
        i.setDateReparation(new Date());
        i.setStatut(InterventionStatus.REPARE);
        Intervention saved = interventionRepository.save(i);
        notificationService.notifyUser(
            saved.getUtilisateur(),
            "Votre intervention est reparee. L'equipement est de nouveau disponible.",
            "INTERVENTION_STATUS",
            "/employe/interventions"
        );
        return toDto(saved);
    }

    public InterventionDto brokenDsn(Long id) {
        Intervention i = interventionRepository.findById(id).orElseThrow();
        if (i.getStatut() != InterventionStatus.EN_ATTENTE_DSN && i.getStatut() != InterventionStatus.EN_COURS) {
            throw new RuntimeException("Bad state");
        }

        List<Equipement> eqs = i.getEquipement() == null ? List.of() : List.copyOf(i.getEquipement());
        for (Equipement eq : eqs) {
            StockEnPanne stock = new StockEnPanne();
            stock.setNomPiece(buildPieceLabel(eq, i));
            stock.setDateAjout(new Date());
            stock.setDemandeurNom(buildDemandeurNom(i.getUtilisateur()));
            stock.setDemandeurTelephone(i.getUtilisateur() == null ? null : i.getUtilisateur().getTelephone());
            stock.setInterventionId(i.getId());
            stockEnPanneRepository.save(stock);

            for (Intervention linked : interventionRepository.findByEquipementId(eq.getId())) {
                if (linked.getEquipement() != null) {
                    linked.getEquipement().removeIf(e -> e.getId().equals(eq.getId()));
                    interventionRepository.save(linked);
                }
            }

            eq.setEvenement(null);
            equipementRepository.delete(eq);
        }

        i.setDsnDecisionAt(new Date());
        i.setStatut(InterventionStatus.CASSE);
        Intervention saved = interventionRepository.save(i);
        notificationService.notifyUser(
            saved.getUtilisateur(),
            "Votre intervention est marquee comme casse. L'equipement est retire du stock disponible.",
            "INTERVENTION_STATUS",
            "/employe/interventions"
        );
        return toDto(saved);
    }

    private String buildPieceLabel(Equipement eq, Intervention i) {
        if (eq != null && eq.getNom() != null && !eq.getNom().isBlank()) {
            if (eq.getNumeroSerie() != null && !eq.getNumeroSerie().isBlank()) {
                return eq.getNom() + " - " + eq.getNumeroSerie();
            }
            return eq.getNom();
        }

        if (eq != null && eq.getTypeEquipement() != null) {
            String label = eq.getTypeEquipement().name();
            if (eq.getNumeroSerie() != null && !eq.getNumeroSerie().isBlank()) {
                return label + " - " + eq.getNumeroSerie();
            }
            return label;
        }

        String type = i.getTypeAppareil();
        String serie = i.getNumeroSerie();
        if (type != null && serie != null && !serie.isBlank()) return type + " - " + serie;
        if (type != null && !type.isBlank()) return type;
        return "Equipement";
    }

    private String buildDemandeurNom(Utilisateur u) {
        if (u == null) return null;
        String nom = u.getNom() == null ? "" : u.getNom().trim();
        String prenom = u.getPrenom() == null ? "" : u.getPrenom().trim();
        String full = (nom + " " + prenom).trim();
        return full.isBlank() ? null : full;
    }

    private InterventionDto toDto(Intervention i) {
        List<Long> eqIds = (i.getEquipement() == null) ? List.of()
                : i.getEquipement().stream().map(Equipement::getId).collect(Collectors.toList());

        return new InterventionDto(
                i.getId(),
            i.getNomDemandeur(),
            i.getDescriptionPanne(),
            i.getTypeAppareil(),
            i.getNumeroSerie(),
            i.getStatut() != null ? i.getStatut().name() : null,
                i.getDateDemande(),
                i.getUtilisateur() != null ? i.getUtilisateur().getId() : null,
            i.getUtilisateur() != null ? i.getUtilisateur().getNom() : null,
            i.getUtilisateur() != null ? i.getUtilisateur().getPrenom() : null,
                i.getUtilisateur() != null ? i.getUtilisateur().getEmail() : null,
            i.getService() != null ? i.getService().getNom() : null,
            eqIds,
            i.getChefCommentaire(),
            i.getRepairMode() != null ? i.getRepairMode().name() : null,
            i.getDsnObservation(),
            i.getDateReparation()
        );
    }
}