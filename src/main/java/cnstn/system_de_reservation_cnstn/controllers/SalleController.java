package cnstn.system_de_reservation_cnstn.controllers;

import cnstn.system_de_reservation_cnstn.dto.salle.SalleRequest;
import cnstn.system_de_reservation_cnstn.dto.salle.SalleResponse;
import cnstn.system_de_reservation_cnstn.services.SalleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/salles", "/Salle"})
@RequiredArgsConstructor
public class SalleController {

    private final SalleService salleService;

    @PostMapping({"", "/add"})
    public SalleResponse create(@RequestBody SalleRequest request) {
        return salleService.create(request);
    }

    @GetMapping({"", "/all"})
    public List<SalleResponse> affiche() {
        return salleService.findAll();
    }

    @PutMapping("/{id}")
    public SalleResponse updateSalle(@PathVariable Long id, @RequestBody SalleRequest request) {
        return salleService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        salleService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}