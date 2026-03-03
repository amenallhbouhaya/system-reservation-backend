package cnstn.system_de_reservation_cnstn.dto;

import cnstn.system_de_reservation_cnstn.models.TypeEvenement;

import java.util.Date;
import java.util.List;

public record EvenementPendingDto(
        Long id,
        String titre,
        Date dateDebut,
        Date dateFin,
        TypeEvenement typeEvenement,
        String statut,
        String salleNom,
        Integer salleCapacite,
        List<String> equipements,
        String demandeurEmail
) {}