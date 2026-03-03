package cnstn.system_de_reservation_cnstn.dto;

import cnstn.system_de_reservation_cnstn.models.TypeEvenement;

import java.util.List;

public record CreateEvenementFullRequest(
        String nom,
        String titre,
        String description,
        Long dateDebut,   // timestamp ms
        Long dateFin,     // timestamp ms
        TypeEvenement typeEvenement,
        Long salleId,             // optional
        List<Long> equipementIds  // optional
) {}