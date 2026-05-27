package cnstn.system_de_reservation_cnstn.controllers;

import cnstn.system_de_reservation_cnstn.models.Utilisateur;
import cnstn.system_de_reservation_cnstn.services.UtilisateurService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/Utilisateur")
public class UtilisateurController {
    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;

    }

    @PostMapping("/add")
    public Utilisateur CreateUtilisateur(@RequestBody Utilisateur utilisateur) {
        return utilisateurService.CreateUtilisateur(utilisateur);
    }

    @GetMapping("/all")
    public List<Utilisateur> afficher()
    {
        return utilisateurService.findAll();
    }
    @GetMapping("/search")
    public List<Utilisateur> rechercherUtilisateur(@RequestParam (required = false) String nom) {
        if (nom == null || nom.isEmpty()) {
            return utilisateurService.findAll();
        }
        return utilisateurService.rechercherParNom(nom);
    }

    @PutMapping("/{id}")
    public Utilisateur updateUtilisateur(@PathVariable Long id, @RequestBody Utilisateur utilisateur) {
        return utilisateurService.updateUtilisateur(id, utilisateur);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable Long id) {
        utilisateurService.deleteById(id);
        return ResponseEntity.ok("Utilisateur supprimé avec succès");
    }
}
