package cnstn.system_de_reservation_cnstn.controllers;

import cnstn.system_de_reservation_cnstn.dto.EquipementAvailabilityDto;
import cnstn.system_de_reservation_cnstn.models.Equipement;
import cnstn.system_de_reservation_cnstn.services.EvenementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/equipements")
@RequiredArgsConstructor
public class ApiEquipementController {

    private final EvenementService evenementService;

    @GetMapping("/available")
    public List<Equipement> available(@RequestParam String start, @RequestParam String end) {
        Date startDate = parseDate(start);
        Date endDate = parseDate(end);
        return evenementService.availableEquipements(startDate, endDate);
    }

    @GetMapping("/availability")
    public List<EquipementAvailabilityDto> availability(@RequestParam String start, @RequestParam String end) {
        Date startDate = parseDate(start);
        Date endDate = parseDate(end);
        return evenementService.equipementsAvailability(startDate, endDate);
    }

    private Date parseDate(String raw) {
        try {
            return Date.from(Instant.parse(raw));
        } catch (Exception ignored) {
            return new Date(Long.parseLong(raw));
        }
    }
}
