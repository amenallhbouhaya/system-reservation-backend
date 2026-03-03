package cnstn.system_de_reservation_cnstn.dto.user;

import cnstn.system_de_reservation_cnstn.models.Role;

public record MeResponse(
        Long id,
        String nom,
        String prenom,
        String email,
        String poste,
        String adresse,
        int telephone,
        int matricule,
        Role role,
        Long serviceId,
        String serviceNom
) {}