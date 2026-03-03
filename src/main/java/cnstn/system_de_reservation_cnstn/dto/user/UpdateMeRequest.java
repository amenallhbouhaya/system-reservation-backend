package cnstn.system_de_reservation_cnstn.dto.user;

public record UpdateMeRequest(
        String nom,
        String prenom,
        String poste,
        String adresse,
        int telephone
) {}