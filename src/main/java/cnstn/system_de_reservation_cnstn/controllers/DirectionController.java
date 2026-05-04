package cnstn.system_de_reservation_cnstn.controllers;

import cnstn.system_de_reservation_cnstn.models.Direction;
import cnstn.system_de_reservation_cnstn.services.DirectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/directions", "/Direction"})
public class DirectionController {
    private final DirectionService directionService;

    public DirectionController(DirectionService directionService) {
        this.directionService = directionService;
    }

    @PostMapping({"", "/add"})
    public Direction createDirection(@RequestBody Direction direction) {
        return directionService.createDirection(direction);
    }

    @GetMapping({"", "/all"})
    public List<Direction> afficher() {
        return directionService.findAll();
    }

    @PutMapping("/{id}")
    public Direction updateDirection(@PathVariable Long id, @RequestBody Direction direction) {
        return directionService.updateDirection(id, direction);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        directionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}