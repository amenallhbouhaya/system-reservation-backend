package cnstn.system_de_reservation_cnstn.controllers;

import cnstn.system_de_reservation_cnstn.dto.auth.PendingRegistrationDto;
import cnstn.system_de_reservation_cnstn.models.PendingRegistration;
import cnstn.system_de_reservation_cnstn.models.Utilisateur;
import cnstn.system_de_reservation_cnstn.repository.PendingRegistrationRepository;
import cnstn.system_de_reservation_cnstn.repository.UtilisateurRepository;
import cnstn.system_de_reservation_cnstn.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chef-hierarchique/users")
@RequiredArgsConstructor
public class ChefHierarchiqueUserController {

    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationService notificationService;

    private static String normalizeRole(String value) {
        if (value == null) return "";
        return value.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
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

    @GetMapping("/pending")
    public List<PendingRegistrationDto> pendingUsers(Authentication auth) {
        assertChefOrAdmin(auth);
        return pendingRegistrationRepository.findAll().stream()
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

    @PostMapping("/{id}/accept")
    public Map<String, String> accept(Authentication auth, @PathVariable Long id) {
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
                "/login"
        );

        return Map.of("status", "ok");
    }

    @PostMapping("/{id}/reject")
    public Map<String, String> reject(Authentication auth, @PathVariable Long id) {
        assertChefOrAdmin(auth);
        PendingRegistration pending = pendingRegistrationRepository.findById(id).orElseThrow();
        pendingRegistrationRepository.delete(pending);
        return Map.of("status", "ok");
    }
}
