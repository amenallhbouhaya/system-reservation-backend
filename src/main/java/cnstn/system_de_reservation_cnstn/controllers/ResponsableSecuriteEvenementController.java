package cnstn.system_de_reservation_cnstn.controllers;

import cnstn.system_de_reservation_cnstn.dto.DecisionRequest;
import cnstn.system_de_reservation_cnstn.dto.EvenementPendingDto;
import cnstn.system_de_reservation_cnstn.models.Evenement;
import cnstn.system_de_reservation_cnstn.services.EvenementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/responsable-securite/evenements")
@RequiredArgsConstructor
public class ResponsableSecuriteEvenementController {

    private final EvenementService evenementService;

    @GetMapping("/pending")
    public List<EvenementPendingDto> pending() {
    return evenementService.pendingForRsec();
    }

    @PutMapping("/{id}/accept")
    public Evenement accept(@PathVariable Long id) {
        return evenementService.rsecAccept(id);
    }

    @PutMapping("/{id}/reject")
    public Evenement reject(@PathVariable Long id, @RequestBody DecisionRequest req) {
        String commentaire = (req == null) ? null : req.commentaire();
        return evenementService.rsecReject(id, commentaire);
    }
}