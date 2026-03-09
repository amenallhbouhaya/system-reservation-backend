package cnstn.system_de_reservation_cnstn.controllers;

import cnstn.system_de_reservation_cnstn.models.Salle;
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
@RequestMapping("/api/salles")
@RequiredArgsConstructor
public class ApiSalleController {

    private final EvenementService evenementService;

    @GetMapping("/available")
    public List<Salle> available(@RequestParam String start, @RequestParam String end) {
        Date startDate = parseDate(start);
        Date endDate = parseDate(end);
        return evenementService.availableSalles(startDate, endDate);
    }

    private Date parseDate(String raw) {
        try {
            return Date.from(Instant.parse(raw));
        } catch (Exception ignored) {
            return new Date(Long.parseLong(raw));
        }
    }
}
