package cnstn.system_de_reservation_cnstn.services;

import cnstn.system_de_reservation_cnstn.dto.auth.UpdateRoleRequest;
import cnstn.system_de_reservation_cnstn.dto.auth.UtilisateurDto;
import cnstn.system_de_reservation_cnstn.models.Evenement;
import cnstn.system_de_reservation_cnstn.models.Intervention;
import cnstn.system_de_reservation_cnstn.models.Utilisateur;
import cnstn.system_de_reservation_cnstn.repository.AppRoleRepository;
import cnstn.system_de_reservation_cnstn.repository.EvenementInvitationRepository;
import cnstn.system_de_reservation_cnstn.repository.EvenementRepository;
import cnstn.system_de_reservation_cnstn.repository.InterventionRepository;
import cnstn.system_de_reservation_cnstn.repository.NotificationRepository;
import cnstn.system_de_reservation_cnstn.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UtilisateurRepository utilisateurRepository;
    private final AppRoleRepository appRoleRepository;
    private final NotificationRepository notificationRepository;
    private final EvenementInvitationRepository evenementInvitationRepository;
    private final EvenementRepository evenmentRepository;
    private final InterventionRepository interventionRepository;

    public List<UtilisateurDto> allUsers() {
        return utilisateurRepository.findAll().stream()
                .map(AdminUserService::toDto)
                .toList();
    }

    public UtilisateurDto updateRole(Long id, UpdateRoleRequest req) {
        Utilisateur utilisateur = utilisateurRepository.findById(id).orElseThrow();
        if (req == null || req.role() == null || req.role().isBlank()) {
            throw new RuntimeException("Role is required");
        }

        String normalizedRole = req.role().trim();
        if (!appRoleRepository.existsByName(normalizedRole)) {
            throw new RuntimeException("Unknown role: " + normalizedRole);
        }

        utilisateur.setRole(normalizedRole);
        Utilisateur saved = utilisateurRepository.save(utilisateur);
        return toDto(saved);
    }

    @Transactional
    public Map<String, String> deleteUser(Long id, String authenticatedEmail) {
        Utilisateur target = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        if (authenticatedEmail != null && authenticatedEmail.equalsIgnoreCase(target.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Suppression de votre propre compte interdite");
        }

        List<Evenement> evenements = evenmentRepository.findByUtilisateurId(id);
        for (Evenement evenement : evenements) {
            evenement.setUtilisateur(null);
        }
        if (!evenements.isEmpty()) {
            evenmentRepository.saveAll(evenements);
        }

        List<Intervention> interventions = interventionRepository.findByUtilisateurId(id);
        for (Intervention intervention : interventions) {
            intervention.setUtilisateur(null);
        }
        if (!interventions.isEmpty()) {
            interventionRepository.saveAll(interventions);
        }

        notificationRepository.deleteByUtilisateurId(id);
        evenementInvitationRepository.deleteByUtilisateurId(id);

        if (target.getDocuments() != null && !target.getDocuments().isEmpty()) {
            target.getDocuments().clear();
            utilisateurRepository.save(target);
        }

        utilisateurRepository.delete(target);

        return Map.of("status", "ok", "message", "Utilisateur supprime avec succes");
    }

    private static UtilisateurDto toDto(Utilisateur utilisateur) {
        return new UtilisateurDto(
                utilisateur.getId(),
                utilisateur.getNom(),
                utilisateur.getPrenom(),
                utilisateur.getEmail(),
                utilisateur.getRole(),
                utilisateur.getMatricule(),
                utilisateur.getTelephone()
        );
    }
}
