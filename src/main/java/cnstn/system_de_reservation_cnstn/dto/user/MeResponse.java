package cnstn.system_de_reservation_cnstn.dto.user;

public record MeResponse(
        Long id,
        String nom,
        String prenom,
        String email,
        String poste,
        String adresse,
        int telephone,
        int matricule,
        String role,
        Long serviceId,
        String serviceNom
) {}