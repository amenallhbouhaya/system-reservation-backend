package cnstn.system_de_reservation_cnstn.controllers;

import cnstn.system_de_reservation_cnstn.dto.auth.UpdateRoleRequest;
import cnstn.system_de_reservation_cnstn.dto.auth.UtilisateurDto;
import cnstn.system_de_reservation_cnstn.models.Utilisateur;
import cnstn.system_de_reservation_cnstn.repository.AppRoleRepository;
import cnstn.system_de_reservation_cnstn.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UtilisateurRepository utilisateurRepository;
    private final AppRoleRepository appRoleRepository;

    @GetMapping
    public List<UtilisateurDto> allUsers() {
        return utilisateurRepository.findAll().stream()
                .map(u -> new UtilisateurDto(
                        u.getId(),
                        u.getNom(),
                        u.getPrenom(),
                        u.getEmail(),
                        u.getRole(),
                        u.getMatricule(),
                        u.getTelephone()
                ))
                .toList();
    }

    @PutMapping("/{id}/role")
    public UtilisateurDto updateRole(@PathVariable Long id, @RequestBody UpdateRoleRequest req) {
        Utilisateur u = utilisateurRepository.findById(id).orElseThrow();
        if (req == null || req.role() == null || req.role().isBlank()) {
            throw new RuntimeException("Role is required");
        }

        String normalizedRole = req.role().trim();
        if (!appRoleRepository.existsByName(normalizedRole)) {
            throw new RuntimeException("Unknown role: " + normalizedRole);
        }

        u.setRole(normalizedRole);
        Utilisateur saved = utilisateurRepository.save(u);

        return new UtilisateurDto(
                saved.getId(),
                saved.getNom(),
                saved.getPrenom(),
                saved.getEmail(),
                saved.getRole(),
                saved.getMatricule(),
                saved.getTelephone()
        );
    }
}
