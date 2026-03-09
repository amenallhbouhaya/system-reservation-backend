package cnstn.system_de_reservation_cnstn.services;

import cnstn.system_de_reservation_cnstn.dto.CreateEvenementFullRequest;
import cnstn.system_de_reservation_cnstn.dto.EquipementAvailabilityDto;
import cnstn.system_de_reservation_cnstn.models.*;
import cnstn.system_de_reservation_cnstn.repository.EquipementRepository;
import cnstn.system_de_reservation_cnstn.repository.EvenmentRepository;
import cnstn.system_de_reservation_cnstn.repository.SaleRepository;
import cnstn.system_de_reservation_cnstn.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;

@Service
@RequiredArgsConstructor
public class EvenementService {

    private final EvenmentRepository evenmentRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final SaleRepository saleRepository;
    private final EquipementRepository equipementRepository;
    private final NotificationService notificationService;
        private static final List<EvenementStatut> STATUTS_NON_BLOQUANTS = List.of(
            EvenementStatut.REFUSE_RSALLE,
            EvenementStatut.REFUSE_RSEC,
            EvenementStatut.REFUSE_DSN
        );

    private String trackingPathByRole(String role) {
        if (role == null) return "/employe/evenements";
        return switch (role) {
            case "ResponsableSalle" -> "/responsable-salle/mes-evenements";
            case "ResponsableSecurite" -> "/responsable-securite/mes-evenements";
            case "DirecteurDsn" -> "/directeur-dsn/mes-evenements";
            default -> "/employe/evenements";
        };
    }

    private void notifyOwnerProgress(Evenement event, String message) {
        Utilisateur owner = event.getUtilisateur();
        if (owner == null) return;
        notificationService.notifyUser(
                owner,
                message,
                "EVENEMENT_PROGRESS",
                trackingPathByRole(owner.getRole())
        );
    }

    private String buildRejectNotification(String actorLabel, String commentaire) {
        String note = (commentaire == null) ? "" : commentaire.trim();
        if (note.isEmpty()) {
            return "Votre événement a été refusé par " + actorLabel + ".";
        }
        return "Votre événement a été refusé par " + actorLabel + ". Motif: " + note;
    }

    // CRUD
    public Evenement create(Evenement evenement) {
        return evenmentRepository.save(evenement);
    }

        @Transactional(readOnly = true)
        public List<String> reservedSlotsByDay(Date dayStart, Date dayEnd) {
        List<Evenement> overlaps = evenmentRepository
            .findByDateDebutLessThanAndDateFinGreaterThanAndStatutNotIn(
                dayEnd,
                dayStart,
                STATUTS_NON_BLOQUANTS
            );

        SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");
        Set<String> slots = new LinkedHashSet<>();
        for (Evenement e : overlaps) {
            if (e.getDateDebut() != null) {
            slots.add(fmt.format(e.getDateDebut()));
            }
        }
        return slots.stream().toList();
        }

        @Transactional(readOnly = true)
        public List<Salle> availableSalles(Date start, Date end) {
        List<Salle> allSalles = saleRepository.findAll();

        List<Evenement> overlaps = evenmentRepository
            .findByDateDebutLessThanAndDateFinGreaterThanAndStatutNotIn(
                end,
                start,
                STATUTS_NON_BLOQUANTS
            );

        Set<Long> occupiedSalleIds = overlaps.stream()
            .flatMap(ev -> ev.getSalle() == null ? java.util.stream.Stream.<Salle>empty() : ev.getSalle().stream())
            .map(Salle::getId)
            .collect(java.util.stream.Collectors.toSet());

        return allSalles.stream()
            .filter(salle -> !occupiedSalleIds.contains(salle.getId()))
            .toList();
        }

        @Transactional(readOnly = true)
        public List<Equipement> availableEquipements(Date start, Date end) {
        List<Evenement> overlaps = evenmentRepository
            .findByDateDebutLessThanAndDateFinGreaterThanAndStatutNotIn(
                end,
                start,
                STATUTS_NON_BLOQUANTS
            );

        Set<Long> occupiedEquipementIds = overlaps.stream()
            .flatMap(ev -> ev.getEquipement() == null ? java.util.stream.Stream.<Equipement>empty() : ev.getEquipement().stream())
            .map(Equipement::getId)
            .collect(java.util.stream.Collectors.toSet());

        return equipementRepository.findAll().stream()
            .filter(eq -> Boolean.TRUE.equals(eq.getReservable()))
            .filter(eq -> !occupiedEquipementIds.contains(eq.getId()))
            .toList();
        }

        @Transactional(readOnly = true)
        public List<EquipementAvailabilityDto> equipementsAvailability(Date start, Date end) {
        List<Evenement> overlaps = evenmentRepository
            .findByDateDebutLessThanAndDateFinGreaterThanAndStatutNotIn(
                end,
                start,
                STATUTS_NON_BLOQUANTS
            );

        Set<Long> occupiedEquipementIds = overlaps.stream()
            .flatMap(ev -> ev.getEquipement() == null ? java.util.stream.Stream.<Equipement>empty() : ev.getEquipement().stream())
            .map(Equipement::getId)
            .collect(java.util.stream.Collectors.toSet());

        return equipementRepository.findAll().stream()
            .filter(eq -> Boolean.TRUE.equals(eq.getReservable()))
            .map(eq -> new EquipementAvailabilityDto(
                eq.getId(),
                eq.getEtat(),
                eq.getReservable(),
                eq.getTypeEquipement(),
                !occupiedEquipementIds.contains(eq.getId())
            ))
            .toList();
        }

        @Transactional(readOnly = true)
        public boolean isSalleOccupiedInRange(Long salleId, Date start, Date end) {
        List<Evenement> overlaps = evenmentRepository
            .findByDateDebutLessThanAndDateFinGreaterThanAndStatutNotIn(
                end,
                start,
                STATUTS_NON_BLOQUANTS
            );

        return overlaps.stream()
            .flatMap(ev -> ev.getSalle() == null ? java.util.stream.Stream.<Salle>empty() : ev.getSalle().stream())
            .anyMatch(salle -> salleId.equals(salle.getId()));
        }

        @Transactional(readOnly = true)
        public boolean isEquipementOccupiedInRange(Long equipementId, Date start, Date end) {
        List<Evenement> overlaps = evenmentRepository
            .findByDateDebutLessThanAndDateFinGreaterThanAndStatutNotIn(
                end,
                start,
                STATUTS_NON_BLOQUANTS
            );

        return overlaps.stream()
            .flatMap(ev -> ev.getEquipement() == null ? java.util.stream.Stream.<Equipement>empty() : ev.getEquipement().stream())
            .anyMatch(eq -> equipementId.equals(eq.getId()));
        }

    public List<Evenement> findAll() {
        return evenmentRepository.findAll();
    }

    public Evenement updateEvenement(Long id, Evenement evenement) {
        Evenement existing = evenmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evenement not found with id: " + id));

        existing.setNom(evenement.getNom());
        existing.setTitre(evenement.getTitre());
        existing.setDescription(evenement.getDescription());
        existing.setDateDebut(evenement.getDateDebut());
        existing.setDateFin(evenement.getDateFin());
        existing.setTypeEvenement(evenement.getTypeEvenement());

        return evenmentRepository.save(existing);
    }

    @Transactional
    public void deleteById(Long id) {
        releaseResources(id);
        evenmentRepository.deleteById(id);
    }

    // ✅ Create Event + reserve salle/equipements + createurRole + statut initial
    @Transactional
    public Evenement createFull(Authentication auth, CreateEvenementFullRequest req) {

        if (req.dateDebut() == null || req.dateFin() == null)
            throw new RuntimeException("Dates obligatoires");

        if (req.dateDebut() >= req.dateFin())
            throw new RuntimeException("dateDebut doit être < dateFin");

        String email = auth.getName();
        Utilisateur u = utilisateurRepository.findByEmail(email).orElseThrow();

        Evenement e = new Evenement();
        e.setNom(req.nom());
        e.setTitre(req.titre());
        e.setDescription(req.description());
        e.setDateDebut(new Date(req.dateDebut()));
        e.setDateFin(new Date(req.dateFin()));
        e.setTypeEvenement(req.typeEvenement());
        e.setUtilisateur(u);

        // ✅ مهمين للـworkflow اللي تحب عليه
        e.setCreateurRole(u.getRole());

        // ✅ statut initial (حسب اللي اتفقنا: RSalle يبدأ عند Rsec، غيره يبدأ عند RSalle)
        if ("ResponsableSalle".equals(u.getRole())) {
            e.setStatut(EvenementStatut.EN_ATTENTE_RSEC);
        } else {
            e.setStatut(EvenementStatut.EN_ATTENTE_RSALLE);
        }

        // Save event باش ياخذ ID
        e = evenmentRepository.save(e);

        // Salle (اختياري)
        if (req.salleId() != null) {
            Salle s = saleRepository.findById(req.salleId()).orElseThrow();

            if (isSalleOccupiedInRange(s.getId(), e.getDateDebut(), e.getDateFin()))
                throw new RuntimeException("Salle déjà réservée dans ce créneau");

            s.setEvenement(e);
            saleRepository.save(s);
        }

        // Equipements (اختياري)
        if (req.equipementIds() != null) {
            for (Long eqId : req.equipementIds()) {
                Equipement eq = equipementRepository.findById(eqId).orElseThrow();

                if (Boolean.FALSE.equals(eq.getReservable()))
                    throw new RuntimeException("Equipement non reservable");

                if (isEquipementOccupiedInRange(eq.getId(), e.getDateDebut(), e.getDateFin()))
                    throw new RuntimeException("Equipement déjà réservé dans ce créneau");

                eq.setEvenement(e);
                equipementRepository.save(eq);
            }
        }

        if (e.getStatut() == EvenementStatut.EN_ATTENTE_RSALLE) {
            notificationService.notifyRole(
                    "ResponsableSalle",
                    "Nouvel événement à valider: " + e.getTitre(),
                    "EVENEMENT_PENDING",
                    "/responsable-salle/evenements"
            );
            notifyOwnerProgress(e, "Votre événement est envoyé au Responsable Salle.");
        } else if (e.getStatut() == EvenementStatut.EN_ATTENTE_RSEC) {
            notificationService.notifyRole(
                    "ResponsableSecurite",
                    "Nouvel événement à valider: " + e.getTitre(),
                    "EVENEMENT_PENDING",
                    "/responsable-securite/evenements"
            );
            notifyOwnerProgress(e, "Votre événement est envoyé au Responsable Sécurité.");
        }

        return e;
    }

    public List<Evenement> myEvents(String email) {
        return evenmentRepository.findByUtilisateurEmail(email);
    }

    // فكّ الموارد
    @Transactional
    public void releaseResources(Long eventId) {
        var salles = saleRepository.findByEvenementId(eventId);
        for (var s : salles) s.setEvenement(null);
        saleRepository.saveAll(salles);

        var eqs = equipementRepository.findByEvenementId(eventId);
        for (var e : eqs) e.setEvenement(null);
        equipementRepository.saveAll(eqs);
    }

    // ====== Workflow decisions (كيف عندك) ======
    @Transactional
    public Evenement rsalleAccept(Long id) {
        Evenement e = evenmentRepository.findById(id).orElseThrow();

        if (e.getStatut() != EvenementStatut.EN_ATTENTE_RSALLE)
            throw new RuntimeException("Bad state");

        // إذا creator هو Rsec => بعد RSalle يمشي مباشرة للـDSN
        if ("ResponsableSecurite".equals(e.getCreateurRole())) {
            e.setStatut(EvenementStatut.EN_ATTENTE_DSN);
            notificationService.notifyRole(
                "DirecteurDsn",
                    "Événement en attente de décision DSN: " + e.getTitre(),
                    "EVENEMENT_PENDING",
                    "/directeur-dsn/evenements"
            );
            notifyOwnerProgress(e, "Votre événement est maintenant chez le Directeur DSN.");
        } else {
            // Employe أو DSN => يمشي للـRsec
            e.setStatut(EvenementStatut.EN_ATTENTE_RSEC);
            notificationService.notifyRole(
                    "ResponsableSecurite",
                    "Événement en attente de validation sécurité: " + e.getTitre(),
                    "EVENEMENT_PENDING",
                    "/responsable-securite/evenements"
            );
            notifyOwnerProgress(e, "Votre événement est maintenant chez le Responsable Sécurité.");
        }

        return evenmentRepository.save(e);
    }

    @Transactional
    public Evenement rsalleReject(Long id, String commentaire) {
        Evenement e = evenmentRepository.findById(id).orElseThrow();
        if (e.getStatut() != EvenementStatut.EN_ATTENTE_RSALLE) throw new RuntimeException("Bad state");

        e.setStatut(EvenementStatut.REFUSE_RSALLE);
        e.setCommentaire(commentaire);
        evenmentRepository.save(e);

        notifyOwnerProgress(e, buildRejectNotification("le Responsable Salle", commentaire));

        releaseResources(id);
        return e;
    }

    @Transactional
    public Evenement rsecAccept(Long id) {
        Evenement e = evenmentRepository.findById(id).orElseThrow();

        if (e.getStatut() != EvenementStatut.EN_ATTENTE_RSEC)
            throw new RuntimeException("Bad state");

        // إذا creator هو DSN => بعد RSalle ثم Rsec => APPROUVE
        if ("DirecteurDsn".equals(e.getCreateurRole())) {
            e.setStatut(EvenementStatut.APPROUVE);
            notifyOwnerProgress(e, "Votre événement est approuvé.");
        } else {
            // Employe أو RSalle => يمشي للـDSN
            e.setStatut(EvenementStatut.EN_ATTENTE_DSN);
            notificationService.notifyRole(
                    "DirecteurDsn",
                    "Événement en attente de décision DSN: " + e.getTitre(),
                    "EVENEMENT_PENDING",
                    "/directeur-dsn/evenements"
            );
            notifyOwnerProgress(e, "Votre événement est maintenant chez le Directeur DSN.");
        }

        return evenmentRepository.save(e);
    }

    @Transactional
    public Evenement rsecReject(Long id, String commentaire) {
        Evenement e = evenmentRepository.findById(id).orElseThrow();
        if (e.getStatut() != EvenementStatut.EN_ATTENTE_RSEC) throw new RuntimeException("Bad state");

        e.setStatut(EvenementStatut.REFUSE_RSEC);
        e.setCommentaire(commentaire);
        evenmentRepository.save(e);

        notifyOwnerProgress(e, buildRejectNotification("le Responsable Sécurité", commentaire));

        releaseResources(id);
        return e;
    }

    @Transactional
    public Evenement dsnAccept(Long id) {
        Evenement e = evenmentRepository.findById(id).orElseThrow();

        if (e.getStatut() != EvenementStatut.EN_ATTENTE_DSN)
            throw new RuntimeException("Bad state");

        e.setStatut(EvenementStatut.APPROUVE);
        notifyOwnerProgress(e, "Votre événement est approuvé par le Directeur DSN.");
        return evenmentRepository.save(e);
    }

    @Transactional
    public Evenement dsnReject(Long id, String commentaire) {
        Evenement e = evenmentRepository.findById(id).orElseThrow();
        if (e.getStatut() != EvenementStatut.EN_ATTENTE_DSN) throw new RuntimeException("Bad state");

        e.setStatut(EvenementStatut.REFUSE_DSN);
        e.setCommentaire(commentaire);
        evenmentRepository.save(e);

        notifyOwnerProgress(e, buildRejectNotification("le Directeur DSN", commentaire));

        releaseResources(id);
        return e;
    }
}