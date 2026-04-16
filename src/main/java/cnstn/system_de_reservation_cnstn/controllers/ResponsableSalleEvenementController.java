package cnstn.system_de_reservation_cnstn.controllers;

import cnstn.system_de_reservation_cnstn.dto.DecisionRequest;
import cnstn.system_de_reservation_cnstn.dto.EvenementAgendaDto;
import cnstn.system_de_reservation_cnstn.dto.EvenementPendingDto;
import cnstn.system_de_reservation_cnstn.models.Evenement;
import cnstn.system_de_reservation_cnstn.services.EvenementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/responsable-salle/evenements")
@RequiredArgsConstructor
public class ResponsableSalleEvenementController {

    private final EvenementService evenementService;

    @GetMapping("/pending")
    public List<EvenementPendingDto> pending() {
    return evenementService.pendingForRsalle();
    }

    @PutMapping("/{id}/accept")
    public Evenement accept(@PathVariable Long id) {
        return evenementService.rsalleAccept(id);
    }

    @PutMapping("/{id}/reject")
    public Evenement reject(@PathVariable Long id, @RequestBody DecisionRequest req) {
        return evenementService.rsalleReject(id, req.commentaire());
    }
    @GetMapping("/agenda")
    public List<EvenementAgendaDto> agenda() {
        return evenementService.agenda();
    }
}