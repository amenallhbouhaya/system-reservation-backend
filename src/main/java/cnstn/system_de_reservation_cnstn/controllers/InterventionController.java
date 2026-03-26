package cnstn.system_de_reservation_cnstn.controllers;

import cnstn.system_de_reservation_cnstn.dto.CreateInterventionRequest;
import cnstn.system_de_reservation_cnstn.dto.DsnCompleteInterventionRequest;
import cnstn.system_de_reservation_cnstn.dto.DsnStartInterventionRequest;
import cnstn.system_de_reservation_cnstn.dto.InterventionDto;
import cnstn.system_de_reservation_cnstn.services.InterventionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/interventions")
@RequiredArgsConstructor
public class InterventionController {

    private final InterventionService interventionService;

    @PostMapping
    public InterventionDto create(Authentication auth, @RequestBody CreateInterventionRequest req) {
        return interventionService.create(auth.getName(), req);
    }

    @GetMapping("/me")
    public List<InterventionDto> my(Authentication auth) {
        return interventionService.myInterventions(auth.getName());
    }

    @GetMapping("/chef/pending")
    public List<InterventionDto> pendingChef() {
        return interventionService.pendingChef();
    }

    @PostMapping("/chef/{id}/accept")
    public InterventionDto acceptChef(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String commentaire = body == null ? null : body.get("commentaire");
        return interventionService.acceptChef(id, commentaire);
    }

    @PostMapping("/chef/{id}/reject")
    public InterventionDto rejectChef(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String commentaire = body == null ? null : body.get("commentaire");
        return interventionService.rejectChef(id, commentaire);
    }

    @GetMapping("/dsn/pending")
    public List<InterventionDto> pendingDsn() {
        return interventionService.pendingDsn();
    }

    @PostMapping("/dsn/{id}/start")
    public InterventionDto startDsn(@PathVariable Long id, @RequestBody DsnStartInterventionRequest req) {
        return interventionService.startDsn(id, req);
    }

    @PostMapping("/dsn/{id}/complete")
    public InterventionDto completeDsn(@PathVariable Long id, @RequestBody DsnCompleteInterventionRequest req) {
        return interventionService.completeDsn(id, req);
    }

    @PostMapping("/dsn/{id}/repair")
    public InterventionDto repairDsn(@PathVariable Long id) {
        return interventionService.repairDsn(id);
    }

    @PostMapping("/dsn/{id}/broken")
    public InterventionDto brokenDsn(@PathVariable Long id) {
        return interventionService.brokenDsn(id);
    }
}