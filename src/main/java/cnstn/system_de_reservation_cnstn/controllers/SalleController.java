package cnstn.system_de_reservation_cnstn.controllers;

import cnstn.system_de_reservation_cnstn.models.Salle;
import cnstn.system_de_reservation_cnstn.services.SalleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/Salle")
@RequiredArgsConstructor
public class SalleController {

    private final SalleService salleService;

    @PostMapping("/add")
    public Salle create(@RequestBody Salle salle){
        return salleService.Create(salle);
    }

    @GetMapping("/all")
    public List<Salle> affiche(){
        return salleService.findAll();
    }

    @PutMapping("/{id}")
    public Salle updateSalle(@PathVariable Long id, @RequestBody Salle salle) {
        return salleService.updateSalle(id, salle);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        salleService.deleteById(id);
        return ResponseEntity.noContent().build(); // 204
    }
}