package cnstn.system_de_reservation_cnstn.dto;

public record AdminDashboardStatsDto(
        long evenementsTotal,
        long evenementsApprouves,
        long evenementsEnAttente,
        long evenementsRefuses,
        long interventionsTotal,
        long interventionsEnAttente,
        long sallesTotal,
        long sallesOccupees,
        long sallesDisponibles
) {
}
