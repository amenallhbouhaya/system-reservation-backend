package cnstn.system_de_reservation_cnstn.controllers;

import cnstn.system_de_reservation_cnstn.dto.equipement.EquipementRequest;
import cnstn.system_de_reservation_cnstn.dto.equipement.EquipementResponse;
import cnstn.system_de_reservation_cnstn.services.EquipementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping({"/api/equipements", "/Equipement"})
public class EquipementController {

    private final EquipementService equipementService;

    public EquipementController(EquipementService equipementService){
        this.equipementService = equipementService;
    }

    @PostMapping({"", "/add"})
    public EquipementResponse createEquipement(@RequestBody EquipementRequest request) {
        return equipementService.create(request);
    }

    @GetMapping({"", "/all"})
    public List<EquipementResponse> afficher() {
        return equipementService.findAllResponses();
    }

    @PutMapping("/{id}")
    public EquipementResponse updateEquipement(@PathVariable Long id, @RequestBody EquipementRequest request) {
        return equipementService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        equipementService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
