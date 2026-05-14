package cnstn.system_de_reservation_cnstn.controllers;

import cnstn.system_de_reservation_cnstn.models.Partenaire;
import cnstn.system_de_reservation_cnstn.services.PartenaireService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.util.List;

@RestController
@RequestMapping("/partenaire")
@CrossOrigin(origins = "*")
public class PatenaireController {
    private final PartenaireService partenaireService;
    public PatenaireController(PartenaireService partenaireService) {
        this.partenaireService = partenaireService;
    }
    @PostMapping("/add")
    public Partenaire createPartenaire(@RequestBody Partenaire partenaire){
        return partenaireService.createPartenaire(partenaire);

    }
    @GetMapping("/all")
    public List<Partenaire> findAll() {
        return partenaireService.findAll();
    }

    @GetMapping("/search")
    public List<Partenaire> searchPartenaire(@RequestBody (required = false)Partenaire partenaire){
        return partenaireService.findAll();
    }
    @PutMapping("/{id}")
    public Partenaire updatePartenaire(@PathVariable Long id, @RequestBody Partenaire partenaire){
        return partenaireService.updatePartenaire(id, partenaire);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable Long id) {
        partenaireService.deleteById(id);
        return ResponseEntity.ok("Partenaire supprimé avec succès");
    }
}
