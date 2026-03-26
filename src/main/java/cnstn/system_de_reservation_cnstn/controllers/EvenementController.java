package cnstn.system_de_reservation_cnstn.controllers;

import cnstn.system_de_reservation_cnstn.dto.CreateEvenementFullRequest;
import cnstn.system_de_reservation_cnstn.dto.InviteEmployesRequest;
import cnstn.system_de_reservation_cnstn.models.Equipement;
import cnstn.system_de_reservation_cnstn.models.Evenement;
import cnstn.system_de_reservation_cnstn.models.Salle;
import cnstn.system_de_reservation_cnstn.repository.EquipementRepository;
import cnstn.system_de_reservation_cnstn.repository.SaleRepository;
import cnstn.system_de_reservation_cnstn.services.EvenementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/Evenement")
@RequiredArgsConstructor
public class EvenementController {

    private final EvenementService evenementService;
    private final SaleRepository salleRepository;
    private final EquipementRepository equipementRepository;

    // CRUD متاع admin (ولا تستعملوه للي تحب)
    @PostMapping("/add")
    public Evenement create(@RequestBody Evenement e) {
        return evenementService.create(e);
    }

    @GetMapping("/all")
    public List<Evenement> all() {
        return evenementService.findAll();
    }

    @PutMapping("/{id}")
    public Evenement update(@PathVariable Long id, @RequestBody Evenement e) {
        return evenementService.updateEvenement(id, e);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        evenementService.deleteById(id);
        return ResponseEntity.ok("Evenement supprimé avec succès");
    }

    @PostMapping("/{id}/invite")
    public ResponseEntity<String> inviteEmployes(@PathVariable Long id, @RequestBody InviteEmployesRequest req) {
        evenementService.inviteEmployes(id, req);
        return ResponseEntity.ok("Invitations envoyées");
    }

    // ✅ Employe: create event + reserve salle/equipements

    @PostMapping("/add-full")
    public Evenement createFull(Authentication auth, @RequestBody CreateEvenementFullRequest req) {
        return evenementService.createFull(auth, req);
    }

    @GetMapping("/my")
    public List<Evenement> my(Authentication auth) {
        return evenementService.myEvents(auth.getName());
    }

    // ✅ available resources
    @GetMapping("/salles")
    public List<Salle> sallesDisponibles(
            @RequestParam(required = false) Long dateDebut,
            @RequestParam(required = false) Long dateFin
    ) {
        if (dateDebut == null || dateFin == null) {
            return salleRepository.findByEvenementIsNull();
        }

        Date start = new Date(dateDebut);
        Date end = new Date(dateFin);
        return evenementService.availableSalles(start, end);
    }

    @GetMapping("/reserved-slots")
    public List<String> reservedSlots(@RequestParam String date) {
        LocalDate day = LocalDate.parse(date);
        LocalDateTime startOfDay = day.atStartOfDay();
        LocalDateTime endOfDay = day.plusDays(1).atStartOfDay();

        Date start = Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant());

        return evenementService.reservedSlotsByDay(start, end);
    }

    @GetMapping("/equipements")
    public List<Equipement> equipementsDisponibles() {
        return equipementRepository.findByEvenementIsNullAndReservableTrue();
    }
}