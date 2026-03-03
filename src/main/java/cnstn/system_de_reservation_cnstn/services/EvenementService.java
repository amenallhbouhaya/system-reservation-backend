package cnstn.system_de_reservation_cnstn.services;

import cnstn.system_de_reservation_cnstn.dto.CreateEvenementFullRequest;
import cnstn.system_de_reservation_cnstn.models.*;
import cnstn.system_de_reservation_cnstn.repository.EquipementRepository;
import cnstn.system_de_reservation_cnstn.repository.EvenmentRepository;
import cnstn.system_de_reservation_cnstn.repository.SaleRepository;
import cnstn.system_de_reservation_cnstn.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EvenementService {

    private final EvenmentRepository evenmentRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final SaleRepository saleRepository;
    private final EquipementRepository equipementRepository;

    // CRUD
    public Evenement create(Evenement evenement) {
        return evenmentRepository.save(evenement);
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
        if (u.getRole() == Role.ResponsableSalle) {
            e.setStatut(EvenementStatut.EN_ATTENTE_RSEC);
        } else {
            e.setStatut(EvenementStatut.EN_ATTENTE_RSALLE);
        }

        // Save event باش ياخذ ID
        e = evenmentRepository.save(e);

        // Salle (اختياري)
        if (req.salleId() != null) {
            Salle s = saleRepository.findById(req.salleId()).orElseThrow();

            if (s.getEvenement() != null)
                throw new RuntimeException("Salle déjà réservée");

            s.setEvenement(e);
            saleRepository.save(s);
        }

        // Equipements (اختياري)
        if (req.equipementIds() != null) {
            for (Long eqId : req.equipementIds()) {
                Equipement eq = equipementRepository.findById(eqId).orElseThrow();

                if (Boolean.FALSE.equals(eq.getReservable()))
                    throw new RuntimeException("Equipement non reservable");

                if (eq.getEvenement() != null)
                    throw new RuntimeException("Equipement déjà réservé");

                eq.setEvenement(e);
                equipementRepository.save(eq);
            }
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
        if (e.getCreateurRole() == Role.ResponsableSecurite) {
            e.setStatut(EvenementStatut.EN_ATTENTE_DSN);
        } else {
            // Employe أو DSN => يمشي للـRsec
            e.setStatut(EvenementStatut.EN_ATTENTE_RSEC);
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

        releaseResources(id);
        return e;
    }

    @Transactional
    public Evenement rsecAccept(Long id) {
        Evenement e = evenmentRepository.findById(id).orElseThrow();

        if (e.getStatut() != EvenementStatut.EN_ATTENTE_RSEC)
            throw new RuntimeException("Bad state");

        // إذا creator هو DSN => بعد RSalle ثم Rsec => APPROUVE
        if (e.getCreateurRole() == Role.DirecteurDsn) {
            e.setStatut(EvenementStatut.APPROUVE);
        } else {
            // Employe أو RSalle => يمشي للـDSN
            e.setStatut(EvenementStatut.EN_ATTENTE_DSN);
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

        releaseResources(id);
        return e;
    }

    @Transactional
    public Evenement dsnAccept(Long id) {
        Evenement e = evenmentRepository.findById(id).orElseThrow();

        if (e.getStatut() != EvenementStatut.EN_ATTENTE_DSN)
            throw new RuntimeException("Bad state");

        e.setStatut(EvenementStatut.APPROUVE);
        return evenmentRepository.save(e);
    }

    @Transactional
    public Evenement dsnReject(Long id, String commentaire) {
        Evenement e = evenmentRepository.findById(id).orElseThrow();
        if (e.getStatut() != EvenementStatut.EN_ATTENTE_DSN) throw new RuntimeException("Bad state");

        e.setStatut(EvenementStatut.REFUSE_DSN);
        e.setCommentaire(commentaire);
        evenmentRepository.save(e);

        releaseResources(id);
        return e;
    }
}