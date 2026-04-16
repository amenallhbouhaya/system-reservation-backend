package cnstn.system_de_reservation_cnstn.services;

import cnstn.system_de_reservation_cnstn.dto.auth.PendingRegistrationDto;
import cnstn.system_de_reservation_cnstn.models.PendingRegistration;
import cnstn.system_de_reservation_cnstn.models.Utilisateur;
import cnstn.system_de_reservation_cnstn.repository.PendingRegistrationRepository;
import cnstn.system_de_reservation_cnstn.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChefHierarchiqueUserService {

    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;

    public List<PendingRegistrationDto> pendingUsers(Authentication auth) {
        assertChefOrAdmin(auth);
        return pendingRegistrationRepository.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getEmailVerified()))
                .map(p -> new PendingRegistrationDto(
                        p.getId(),
                        p.getNom(),
                        p.getPrenom(),
                        p.getEmail(),
                        p.getMatricule(),
                        p.getTelephone(),
                        p.getDateCreation()
                ))
                .toList();
    }

    @Transactional
    public Map<String, String> accept(Authentication auth, Long id) {
        assertChefOrAdmin(auth);
        PendingRegistration pending = pendingRegistrationRepository.findById(id).orElseThrow();

        if (utilisateurRepository.existsByEmail(pending.getEmail())) {
            pendingRegistrationRepository.delete(pending);
            return Map.of("status", "ok", "message", "Compte déjà existant, demande supprimée");
        }

        Utilisateur user = new Utilisateur();
        user.setNom(pending.getNom());
        user.setPrenom(pending.getPrenom());
        user.setEmail(pending.getEmail());
        user.setPassword(pending.getPassword());
        user.setRole("Employe");
        user.setMatricule(pending.getMatricule());
        user.setTelephone(pending.getTelephone());
        utilisateurRepository.save(user);

        pendingRegistrationRepository.delete(pending);

        notificationService.notifyUser(
                user,
                "Votre compte a été accepté par le chef hiérarchique. Vous pouvez vous connecter.",
                "ACCOUNT_APPROVED",
                "/"
        );

        boolean emailSent = emailService.sendSimpleEmail(
                user.getEmail(),
                "Validation de votre compte CNSTN",
                buildAccountApprovedMailBody(user.getPrenom())
        );

        String message = emailSent
                ? "Compte accepte. Un email de confirmation a ete envoye a l'utilisateur."
                : "Compte accepte, mais l'email de confirmation n'a pas pu etre envoye.";

        return Map.of("status", "ok", "message", message);
    }

    @Transactional
    public Map<String, String> reject(Authentication auth, Long id) {
        assertChefOrAdmin(auth);
        PendingRegistration pending = pendingRegistrationRepository.findById(id).orElseThrow();

        boolean emailSent = emailService.sendSimpleEmail(
                pending.getEmail(),
                "Decision sur votre demande de compte CNSTN",
                buildAccountRejectedMailBody(pending.getPrenom())
        );

        pendingRegistrationRepository.delete(pending);

        String message = emailSent
                ? "Demande refusee. Un email d'information a ete envoye."
                : "Demande refusee, mais l'email d'information n'a pas pu etre envoye.";

        return Map.of("status", "ok", "message", message);
    }

    private static String normalizeRole(String value) {
        if (value == null) return "";
        return value.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }

    private static String buildAccountApprovedMailBody(String prenom) {
        String firstName = (prenom == null || prenom.isBlank()) ? "" : (" " + prenom.trim());
        return "Bonjour" + firstName + ",\n\n"
                + "Votre compte CNSTN a ete valide par le chef hierarchique.\n"
                + "Vous pouvez maintenant vous connecter a la plateforme.\n\n"
                + "Cordialement,\n"
                + "CNSTN";
    }

    private static String buildAccountRejectedMailBody(String prenom) {
        String firstName = (prenom == null || prenom.isBlank()) ? "" : (" " + prenom.trim());
        return "Bonjour" + firstName + ",\n\n"
                + "Votre demande de creation de compte CNSTN a ete refusee.\n"
                + "Pour plus d'informations, merci de contacter votre chef hierarchique.\n\n"
                + "Cordialement,\n"
                + "CNSTN";
    }

    private void assertChefOrAdmin(Authentication auth) {
        if (auth == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé");
        }

        boolean allowed = auth.getAuthorities().stream().anyMatch(a -> {
            String role = a.getAuthority();
            if (role != null && role.startsWith("ROLE_")) {
                role = role.substring(5);
            }
            String normalized = normalizeRole(role);
            return normalized.equals("chefhierarchique") || normalized.equals("admin");
        });

        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé pour ce rôle");
        }
    }
}
