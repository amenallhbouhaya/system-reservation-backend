package cnstn.system_de_reservation_cnstn.dto.salle;

public record SalleResponse(
        Long id,
        String nom,
        int capacite,
        String description
) {
}