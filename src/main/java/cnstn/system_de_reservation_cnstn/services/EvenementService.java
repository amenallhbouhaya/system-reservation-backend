package cnstn.system_de_reservation_cnstn.services;

import cnstn.system_de_reservation_cnstn.dto.CreateEvenementFullRequest;
import cnstn.system_de_reservation_cnstn.dto.ExternalPartnerRequest;
import cnstn.system_de_reservation_cnstn.dto.InviteEmployesRequest;
import cnstn.system_de_reservation_cnstn.dto.InvitationViewDto;
import cnstn.system_de_reservation_cnstn.dto.InvitationCheckRequest;
import cnstn.system_de_reservation_cnstn.dto.InvitationCheckResponse;
import cnstn.system_de_reservation_cnstn.repository.EvenementInvitationRepository;
import cnstn.system_de_reservation_cnstn.repository.EvenementExternalInvitationRepository;
import cnstn.system_de_reservation_cnstn.dto.EquipementAvailabilityDto;
import cnstn.system_de_reservation_cnstn.models.*;
import cnstn.system_de_reservation_cnstn.repository.EquipementRepository;
import cnstn.system_de_reservation_cnstn.repository.EvenmentRepository;
import cnstn.system_de_reservation_cnstn.repository.InterventionRepository;
import cnstn.system_de_reservation_cnstn.repository.SaleRepository;
import cnstn.system_de_reservation_cnstn.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvenementService {

    private final EvenmentRepository evenmentRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final SaleRepository saleRepository;
    private final EquipementRepository equipementRepository;
    private final NotificationService notificationService;
    private final EvenementInvitationRepository invitationRepository;
    private final EvenementExternalInvitationRepository externalInvitationRepository;
    private final EmailService emailService;
    private final InterventionRepository interventionRepository;
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

    private String buildInviteMessage(Evenement e, String referenceCode) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String dateStart = e.getDateDebut() != null ? fmt.format(e.getDateDebut()) : "";
        String dateEnd = e.getDateFin() != null ? fmt.format(e.getDateFin()) : "";

        String creator = "";
        if (e.getUtilisateur() != null) {
            String nom = e.getUtilisateur().getNom() == null ? "" : e.getUtilisateur().getNom();
            String prenom = e.getUtilisateur().getPrenom() == null ? "" : e.getUtilisateur().getPrenom();
            creator = (nom + " " + prenom).trim();
        }

        String salleNames = "";
        if (e.getSalle() != null && !e.getSalle().isEmpty()) {
            salleNames = e.getSalle().stream()
                .map(Salle::getNom)
                .filter(n -> n != null && !n.isBlank())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        }

        return "Convocation: " + e.getTitre()
            + (creator.isEmpty() ? "" : " | Organisateur: " + creator)
            + (salleNames.isEmpty() ? "" : " | Salle: " + salleNames)
            + (dateStart.isEmpty() && dateEnd.isEmpty() ? "" : " | " + dateStart + " - " + dateEnd)
            + (referenceCode == null || referenceCode.isBlank() ? "" : " | Ref: " + referenceCode);
    }

    private String toReference(Long invitationId) {
        if (invitationId == null) return "";
        return "CNSTN-" + String.format("%04d", invitationId);
    }

    private String toExternalReference(Long invitationId) {
        if (invitationId == null) return "";
        return "CNSTN-EXT-" + String.format("%04d", invitationId);
    }

    private EvenementInvitation createInvitation(Evenement e, Utilisateur user) {
        EvenementInvitation inv = new EvenementInvitation();
        inv.setEvenement(e);
        inv.setUtilisateur(user);
        inv.setCreatedAt(new Date());
        inv = invitationRepository.save(inv);
        inv.setReferenceCode(toReference(inv.getId()));
        return invitationRepository.save(inv);
    }

    private EvenementExternalInvitation createExternalInvitation(Evenement e, String nom, String email) {
        EvenementExternalInvitation inv = new EvenementExternalInvitation();
        inv.setEvenement(e);
        inv.setInviteNom(nom);
        inv.setInviteEmail(email);
        inv.setCreatedAt(new Date());
        inv = externalInvitationRepository.save(inv);
        inv.setReferenceCode(toExternalReference(inv.getId()));
        return externalInvitationRepository.save(inv);
    }

    private void sendInvitesIfNeeded(Evenement e) {
        if (e == null) return;
        if (Boolean.TRUE.equals(e.getInviteSent())) return;

        boolean inviteAll = Boolean.TRUE.equals(e.getInviteAll());
        List<Long> inviteIds = e.getInvitedUserIds();
        if (!inviteAll && (inviteIds == null || inviteIds.isEmpty())) return;

        List<Utilisateur> targets = inviteAll
            ? utilisateurRepository.findAll()
            : utilisateurRepository.findAllById(inviteIds);


        for (Utilisateur user : targets) {
            EvenementInvitation inv = invitationRepository
                .findByEvenementIdAndUtilisateurId(e.getId(), user.getId())
                .orElseGet(() -> createInvitation(e, user));

            String message = buildInviteMessage(e, inv.getReferenceCode());
            String targetPath = "/invitation/" + inv.getId();

            notificationService.notifyUser(
                user,
                message,
                "EVENEMENT_INVITE",
                targetPath
            );
        }

        e.setInviteSent(true);
    }

    private void sendExternalInvitesIfNeeded(Evenement e) {
        if (e == null) return;

        List<EvenementExternalInvitation> externals = externalInvitationRepository.findByEvenementId(e.getId());
        for (EvenementExternalInvitation inv : externals) {
            if (inv.getSentAt() != null) continue;
            boolean sent = emailService.sendExternalInvitation(inv, e);
            if (sent) {
                inv.setSentAt(new Date());
                externalInvitationRepository.save(inv);
            } else {
                log.warn("External invite not sent. invitationId={} email={}", inv.getId(), inv.getInviteEmail());
            }
        }
    }

    @Transactional(readOnly = true)
    public InvitationViewDto invitationView(Authentication auth, Long evenementId) {
        EvenementInvitation inv = invitationRepository.findById(evenementId).orElseThrow();
        Evenement e = inv.getEvenement();
        Utilisateur me = utilisateurRepository.findByEmail(auth.getName()).orElseThrow();

        if (e.getStatut() != EvenementStatut.APPROUVE) {
            throw new RuntimeException("Invitation non disponible");
        }

        boolean isOwner = inv.getUtilisateur() != null && me.getId().equals(inv.getUtilisateur().getId());
        if (!isOwner) {
            throw new RuntimeException("Invitation non autorisee");
        }

        String salleNames = "";
        if (e.getSalle() != null && !e.getSalle().isEmpty()) {
            salleNames = e.getSalle().stream()
                .map(Salle::getNom)
                .filter(n -> n != null && !n.isBlank())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        }

        String organizateur = "";
        if (e.getUtilisateur() != null) {
            String nom = e.getUtilisateur().getNom() == null ? "" : e.getUtilisateur().getNom();
            String prenom = e.getUtilisateur().getPrenom() == null ? "" : e.getUtilisateur().getPrenom();
            organizateur = (nom + " " + prenom).trim();
        }

        String destinataire = "";
        String email = null;
        Integer telephone = null;
        if (inv.getUtilisateur() != null) {
            String nom = inv.getUtilisateur().getNom() == null ? "" : inv.getUtilisateur().getNom();
            String prenom = inv.getUtilisateur().getPrenom() == null ? "" : inv.getUtilisateur().getPrenom();
            destinataire = (nom + " " + prenom).trim();
            email = inv.getUtilisateur().getEmail();
            telephone = inv.getUtilisateur().getTelephone();
        }

        return new InvitationViewDto(
            e.getId(),
            e.getTitre(),
            e.getDescription(),
            salleNames,
            e.getDateDebut(),
            e.getDateFin(),
            organizateur,
            destinataire,
            inv.getReferenceCode(),
            inv.getUsedAt(),
            email,
            telephone
        );
    }

    @Transactional
    public InvitationCheckResponse checkInvitation(Authentication auth, InvitationCheckRequest req, boolean consume) {
        Utilisateur me = utilisateurRepository.findByEmail(auth.getName()).orElseThrow();
        if (me.getRole() == null || !me.getRole().equals("ResponsableSecurite")) {
            throw new RuntimeException("Forbidden");
        }

        String ref = req == null || req.referenceCode() == null ? "" : req.referenceCode().trim().toUpperCase();
        if (ref.isEmpty()) {
            return new InvitationCheckResponse("NOT_FOUND", "Reference manquante", null, null);
        }

        EvenementInvitation inv = invitationRepository.findByReferenceCode(ref).orElse(null);
        if (inv == null) {
            EvenementExternalInvitation ext = externalInvitationRepository.findByReferenceCode(ref).orElse(null);
            if (ext == null) {
                return new InvitationCheckResponse("NOT_FOUND", "Reference introuvable", null, null);
            }

            Evenement e = ext.getEvenement();
            if (e == null || e.getStatut() != EvenementStatut.APPROUVE) {
                return new InvitationCheckResponse("NOT_READY", "Evenement non approuve", null, null);
            }

            if (ext.getUsedAt() != null) {
                return new InvitationCheckResponse(
                    "ALREADY_USED",
                    "Convocation deja utilisee",
                    toInvitationView(ext),
                    ext.getUsedAt()
                );
            }

            if (consume) {
                ext.setUsedAt(new Date());
                externalInvitationRepository.save(ext);
            }

            return new InvitationCheckResponse(
                "VALID",
                "Convocation valide",
                toInvitationView(ext),
                ext.getUsedAt()
            );
        }

        Evenement e = inv.getEvenement();
        if (e == null || e.getStatut() != EvenementStatut.APPROUVE) {
            return new InvitationCheckResponse("NOT_READY", "Evenement non approuve", null, null);
        }

        if (inv.getUsedAt() != null) {
            return new InvitationCheckResponse(
                "ALREADY_USED",
                "Convocation deja utilisee",
                toInvitationView(inv),
                inv.getUsedAt()
            );
        }

        if (consume) {
            inv.setUsedAt(new Date());
            invitationRepository.save(inv);
        }

        return new InvitationCheckResponse(
            "VALID",
            "Convocation valide",
            toInvitationView(inv),
            inv.getUsedAt()
        );
    }

    private InvitationViewDto toInvitationView(EvenementExternalInvitation inv) {
        Evenement e = inv.getEvenement();
        String salleNames = "";
        if (e != null && e.getSalle() != null && !e.getSalle().isEmpty()) {
            salleNames = e.getSalle().stream()
                .map(Salle::getNom)
                .filter(n -> n != null && !n.isBlank())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        }

        String organizateur = "";
        if (e != null && e.getUtilisateur() != null) {
            String nom = e.getUtilisateur().getNom() == null ? "" : e.getUtilisateur().getNom();
            String prenom = e.getUtilisateur().getPrenom() == null ? "" : e.getUtilisateur().getPrenom();
            organizateur = (nom + " " + prenom).trim();
        }

        String destinataire = inv.getInviteNom() == null ? "" : inv.getInviteNom().trim();
        String email = inv.getInviteEmail();

        return new InvitationViewDto(
            e == null ? null : e.getId(),
            e == null ? "" : e.getTitre(),
            e == null ? null : e.getDescription(),
            salleNames,
            e == null ? null : e.getDateDebut(),
            e == null ? null : e.getDateFin(),
            organizateur,
            destinataire,
            inv.getReferenceCode(),
            inv.getUsedAt(),
            email,
            null
        );
    }

    private InvitationViewDto toInvitationView(EvenementInvitation inv) {
        Evenement e = inv.getEvenement();
        String salleNames = "";
        if (e != null && e.getSalle() != null && !e.getSalle().isEmpty()) {
            salleNames = e.getSalle().stream()
                .map(Salle::getNom)
                .filter(n -> n != null && !n.isBlank())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        }

        String organizateur = "";
        if (e != null && e.getUtilisateur() != null) {
            String nom = e.getUtilisateur().getNom() == null ? "" : e.getUtilisateur().getNom();
            String prenom = e.getUtilisateur().getPrenom() == null ? "" : e.getUtilisateur().getPrenom();
            organizateur = (nom + " " + prenom).trim();
        }

        String destinataire = "";
        String email = null;
        Integer telephone = null;
        if (inv.getUtilisateur() != null) {
            String nom = inv.getUtilisateur().getNom() == null ? "" : inv.getUtilisateur().getNom();
            String prenom = inv.getUtilisateur().getPrenom() == null ? "" : inv.getUtilisateur().getPrenom();
            destinataire = (nom + " " + prenom).trim();
            email = inv.getUtilisateur().getEmail();
            telephone = inv.getUtilisateur().getTelephone();
        }

        return new InvitationViewDto(
            e == null ? null : e.getId(),
            e == null ? "" : e.getTitre(),
            e == null ? null : e.getDescription(),
            salleNames,
            e == null ? null : e.getDateDebut(),
            e == null ? null : e.getDateFin(),
            organizateur,
            destinataire,
            inv.getReferenceCode(),
            inv.getUsedAt(),
            email,
            telephone
        );
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
            .filter(eq -> interventionRepository.findByEquipementIdAndStatutIn(
                eq.getId(),
                Set.of(
                    InterventionStatus.EN_ATTENTE_CHEF,
                    InterventionStatus.EN_ATTENTE_DSN,
                    InterventionStatus.EN_COURS
                )
            ).isEmpty())
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
            !occupiedEquipementIds.contains(eq.getId()) && interventionRepository.findByEquipementIdAndStatutIn(
                eq.getId(),
                Set.of(
                    InterventionStatus.EN_ATTENTE_CHEF,
                    InterventionStatus.EN_ATTENTE_DSN,
                    InterventionStatus.EN_COURS
                )
            ).isEmpty()
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
        e.setLienEnLigne(req.lienEnLigne());
        e.setUtilisateur(u);
        e.setInviteAll(req.inviteAll());
        e.setInvitedUserIds(req.inviteUserIds() == null ? new java.util.ArrayList<>() : req.inviteUserIds());
        e.setInviteSent(false);

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

        List<ExternalPartnerRequest> externalPartners = new ArrayList<>();
        if (req.partenairesExternes() != null) {
            externalPartners.addAll(req.partenairesExternes());
        }

        String extNom = req.partenaireNom() == null ? "" : req.partenaireNom().trim();
        String extEmail = req.partenaireEmail() == null ? "" : req.partenaireEmail().trim();
        if ((!extNom.isEmpty() && extEmail.isEmpty()) || (extNom.isEmpty() && !extEmail.isEmpty())) {
            throw new RuntimeException("Nom et email partenaire obligatoires");
        }
        if (!extNom.isEmpty()) {
            externalPartners.add(new ExternalPartnerRequest(extNom, extEmail));
        }

        Set<String> seenExternal = new LinkedHashSet<>();
        for (ExternalPartnerRequest p : externalPartners) {
            String nom = p == null || p.nom() == null ? "" : p.nom().trim();
            String emailExt = p == null || p.email() == null ? "" : p.email().trim();

            if (nom.isEmpty() && emailExt.isEmpty()) continue;
            if (nom.isEmpty() || emailExt.isEmpty()) {
                throw new RuntimeException("Nom et email partenaire obligatoires");
            }

            String dedupeKey = (nom + "|" + emailExt).toLowerCase();
            if (seenExternal.add(dedupeKey)) {
                createExternalInvitation(e, nom, emailExt);
            }
        }

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

    @Transactional
    public void inviteEmployes(Long evenementId, InviteEmployesRequest req) {
        Evenement e = evenmentRepository.findById(evenementId).orElseThrow();

        if (req == null) return;

        List<Utilisateur> targets;
        if (req.inviteAll()) {
            targets = utilisateurRepository.findAll();
        } else if (req.userIds() != null && !req.userIds().isEmpty()) {
            targets = utilisateurRepository.findAllById(req.userIds());
        } else {
            return;
        }

        for (Utilisateur user : targets) {
            EvenementInvitation inv = invitationRepository
                .findByEvenementIdAndUtilisateurId(e.getId(), user.getId())
                .orElseGet(() -> createInvitation(e, user));

            String message = buildInviteMessage(e, inv.getReferenceCode());
            String targetPath = "/invitation/" + inv.getId();

            notificationService.notifyUser(
                user,
                message,
                "EVENEMENT_INVITE",
                targetPath
            );
        }

        e.setInviteSent(true);
        evenmentRepository.save(e);
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
            sendInvitesIfNeeded(e);
            sendExternalInvitesIfNeeded(e);
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
        sendInvitesIfNeeded(e);
        sendExternalInvitesIfNeeded(e);
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