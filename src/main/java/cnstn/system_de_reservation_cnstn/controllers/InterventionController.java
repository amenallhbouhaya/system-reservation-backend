package cnstn.system_de_reservation_cnstn.controllers;

import cnstn.system_de_reservation_cnstn.dto.CreateInterventionRequest;
import cnstn.system_de_reservation_cnstn.dto.AdminCompleteInterventionRequest;
import cnstn.system_de_reservation_cnstn.dto.AdminStartInterventionRequest;
import cnstn.system_de_reservation_cnstn.dto.InterventionDto;
import cnstn.system_de_reservation_cnstn.services.InterventionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/interventions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InterventionController {

    private final InterventionService interventionService;

    private static String normalizeRole(String value) {
        if (value == null) return "";
        return value.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }

    private boolean hasRole(Authentication auth, String expectedNormalizedRole) {
        if (auth == null || auth.getAuthorities() == null) return false;

        return auth.getAuthorities().stream().anyMatch(a -> {
            String role = a.getAuthority();
            if (role != null && role.startsWith("ROLE_")) {
                role = role.substring(5);
            }
            return normalizeRole(role).equals(expectedNormalizedRole);
        });
    }

    private void assertChefOrAdmin(Authentication auth) {
        if (hasRole(auth, "chefhierarchique") || hasRole(auth, "admin")) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acces refuse pour ce role");
    }

    private void assertAdmin(Authentication auth) {
        if (hasRole(auth, "admin")) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acces reserve a l'admin");
    }

    @PostMapping
    public InterventionDto create(Authentication auth, @RequestBody CreateInterventionRequest req) {
        return interventionService.create(auth.getName(), req);
    }

    @GetMapping("/me")
    public List<InterventionDto> my(Authentication auth) {
        return interventionService.myInterventions(auth.getName());
    }

    @GetMapping("/chef/pending")
    public List<InterventionDto> pendingChef(Authentication auth) {
        assertChefOrAdmin(auth);
        return interventionService.pendingChef();
    }

    @PostMapping("/chef/{id}/accept")
    public InterventionDto acceptChef(Authentication auth, @PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        assertChefOrAdmin(auth);
        String commentaire = body == null ? null : body.get("commentaire");
        return interventionService.acceptChef(id, commentaire);
    }

    @PostMapping("/chef/{id}/reject")
    public InterventionDto rejectChef(Authentication auth, @PathVariable Long id, @RequestBody Map<String, String> body) {
        assertChefOrAdmin(auth);
        String commentaire = body == null ? null : body.get("commentaire");
        return interventionService.rejectChef(id, commentaire);
    }

    @GetMapping("/admin/pending")
    public List<InterventionDto> pendingAdmin(Authentication auth) {
        assertAdmin(auth);
        return interventionService.pendingAdmin();
    }

    @PostMapping("/admin/{id}/start")
    public InterventionDto startAdmin(Authentication auth, @PathVariable Long id, @RequestBody AdminStartInterventionRequest req) {
        assertAdmin(auth);
        return interventionService.startAdmin(id, req);
    }

    @PostMapping("/admin/{id}/complete")
    public InterventionDto completeAdmin(Authentication auth, @PathVariable Long id, @RequestBody AdminCompleteInterventionRequest req) {
        assertAdmin(auth);
        return interventionService.completeAdmin(id, req);
    }

    @PostMapping("/admin/{id}/repair")
    public InterventionDto repairAdmin(Authentication auth, @PathVariable Long id) {
        assertAdmin(auth);
        return interventionService.repairAdmin(id);
    }

    @PostMapping("/admin/{id}/broken")
    public InterventionDto brokenAdmin(Authentication auth, @PathVariable Long id) {
        assertAdmin(auth);
        return interventionService.brokenAdmin(id);
    }
}