package cnstn.system_de_reservation_cnstn.controllers;

import cnstn.system_de_reservation_cnstn.models.Services;
import cnstn.system_de_reservation_cnstn.models.Utilisateur;
import cnstn.system_de_reservation_cnstn.repository.ServicesRepository;
import cnstn.system_de_reservation_cnstn.services.ServicesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/services")
public class ServicesController {
    private ServicesService servicesService;
    public ServicesController(ServicesService servicesService) {
        this.servicesService = servicesService;

    }
    @PostMapping("/add")
    public Services addService(@RequestBody Services services) {
        return servicesService.createServices(services);
    }
    @GetMapping("/all")
    public List<Services> afficher(){
        return servicesService.findAllServices();
    }
    @PutMapping("/{id}")
    public Services updateService(@PathVariable Long id, @RequestBody Services service) {
        return servicesService.updateService(id, service);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteService(@PathVariable Long id) {
        servicesService.deleteService(id);
        return ResponseEntity.ok("Service supprimé avec succès");
    }

}
