package cnstn.system_de_reservation_cnstn.services;

import cnstn.system_de_reservation_cnstn.dto.AdminDashboardStatsDto;
import cnstn.system_de_reservation_cnstn.models.EvenementStatut;
import cnstn.system_de_reservation_cnstn.models.InterventionStatus;
import cnstn.system_de_reservation_cnstn.repository.EvenmentRepository;
import cnstn.system_de_reservation_cnstn.repository.InterventionRepository;
import cnstn.system_de_reservation_cnstn.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final EvenmentRepository evenmentRepository;
    private final InterventionRepository interventionRepository;
    private final SaleRepository saleRepository;

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
        long interventionsEnAttente =
                interventionRepository.countByStatut(InterventionStatus.EN_ATTENTE_CHEF)
                        + interventionRepository.countByStatut(InterventionStatus.EN_ATTENTE_ADMIN)
                        + interventionRepository.countByStatut(InterventionStatus.EN_ATTENTE_DSN);

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
