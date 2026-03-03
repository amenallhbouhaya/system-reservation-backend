package cnstn.system_de_reservation_cnstn.controllers;

import cnstn.system_de_reservation_cnstn.dto.CreateInterventionRequest;
import cnstn.system_de_reservation_cnstn.dto.InterventionDto;
import cnstn.system_de_reservation_cnstn.services.InterventionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}