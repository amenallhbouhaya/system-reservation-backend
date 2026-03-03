package cnstn.system_de_reservation_cnstn.dto.auth;

import cnstn.system_de_reservation_cnstn.models.Role;

public record UtilisateurDto(
        Long id,
        String nom,
        String prenom,
        String email,
        Role role,
        Integer matricule,
        Integer telephone
) {}