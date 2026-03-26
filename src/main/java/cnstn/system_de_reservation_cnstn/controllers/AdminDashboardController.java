package cnstn.system_de_reservation_cnstn.controllers;

import cnstn.system_de_reservation_cnstn.dto.AdminDashboardStatsDto;
import cnstn.system_de_reservation_cnstn.models.EvenementStatut;
import cnstn.system_de_reservation_cnstn.repository.EvenmentRepository;
import cnstn.system_de_reservation_cnstn.repository.InterventionRepository;
import cnstn.system_de_reservation_cnstn.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final EvenmentRepository evenmentRepository;
    private final InterventionRepository interventionRepository;
    private final SaleRepository saleRepository;

    @GetMapping("/stats")
    public AdminDashboardStatsDto stats() {
        long evenementsTotal = evenmentRepository.count();
        long evenementsApprouves = evenmentRepository.countByStatut(EvenementStatut.APPROUVE);
        long evenementsEnAttente =
                evenmentRepository.countByStatut(EvenementStatut.EN_ATTENTE_RSALLE)
                        + evenmentRepository.countByStatut(EvenementStatut.EN_ATTENTE_RSEC)
                        + evenmentRepository.countByStatut(EvenementStatut.EN_ATTENTE_DSN);

        long evenementsRefuses =
                evenmentRepository.countByStatut(EvenementStatut.REFUSE_RSALLE)
                        + evenmentRepository.countByStatut(EvenementStatut.REFUSE_RSEC)
                        + evenmentRepository.countByStatut(EvenementStatut.REFUSE_DSN);

        long interventionsTotal = interventionRepository.count();
        long interventionsEnAttente = interventionRepository.countByStatut(cnstn.system_de_reservation_cnstn.models.InterventionStatus.EN_ATTENTE_CHEF);

        long sallesTotal = saleRepository.count();
        long sallesOccupees = saleRepository.countByEvenementIsNotNull();
        long sallesDisponibles = Math.max(0, sallesTotal - sallesOccupees);

        return new AdminDashboardStatsDto(
                evenementsTotal,
                evenementsApprouves,
                evenementsEnAttente,
                evenementsRefuses,
                interventionsTotal,
                interventionsEnAttente,
                sallesTotal,
                sallesOccupees,
                sallesDisponibles
        );
    }
}
