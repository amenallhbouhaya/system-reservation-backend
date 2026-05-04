package cnstn.system_de_reservation_cnstn.dto.salle;

public record SalleRequest(
        String nom,
        int capacite,
        String description
) {
}