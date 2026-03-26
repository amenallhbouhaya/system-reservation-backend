package cnstn.system_de_reservation_cnstn.controllers;

import cnstn.system_de_reservation_cnstn.dto.InvitationCheckRequest;
import cnstn.system_de_reservation_cnstn.dto.InvitationCheckResponse;
import cnstn.system_de_reservation_cnstn.dto.InvitationViewDto;
import cnstn.system_de_reservation_cnstn.services.EvenementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final EvenementService evenementService;

    @GetMapping("/{id}")
    public InvitationViewDto view(Authentication auth, @PathVariable Long id) {
        return evenementService.invitationView(auth, id);
    }

    @PostMapping("/check")
    public InvitationCheckResponse check(Authentication auth, @RequestBody InvitationCheckRequest req) {
        return evenementService.checkInvitation(auth, req, false);
    }

    @PostMapping("/consume")
    public InvitationCheckResponse consume(Authentication auth, @RequestBody InvitationCheckRequest req) {
        return evenementService.checkInvitation(auth, req, true);
    }
}