package cnstn.system_de_reservation_cnstn.controllers;

import cnstn.system_de_reservation_cnstn.models.Document;
import cnstn.system_de_reservation_cnstn.models.Evenement;
import cnstn.system_de_reservation_cnstn.models.Utilisateur;
import cnstn.system_de_reservation_cnstn.repository.DocumentRepository;
import cnstn.system_de_reservation_cnstn.repository.EvenmentRepository;
import cnstn.system_de_reservation_cnstn.repository.UtilisateurRepository;
import cnstn.system_de_reservation_cnstn.services.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/directeur-dsn/documents", "/directeur-dsn/documents"})
@RequiredArgsConstructor
public class DirecteurDsnDocumentController {

    private final EvenmentRepository evenmentRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final DocumentRepository documentRepository;
    private final FileStorageService storage;

    @PostMapping(value = {"/send-to-employes", "/send-to-employees"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> sendToEmployes(
            @RequestPart("file") MultipartFile file,
            @RequestParam("titre") String titre,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "niveauAcces", required = false) String niveauAcces
    ) {
        String filename = storage.store(file);

        Document d = new Document();
        d.setTitre(titre);
        d.setChemin(filename);
        d.setNiveauAcces(niveauAcces);

        String finalType = (type != null && !type.isBlank()) ? type : file.getContentType();
        d.setType(finalType);

        Document saved = documentRepository.save(d);

        List<Utilisateur> employes = utilisateurRepository.findByRole("Employe");
        int assignedCount = 0;

        for (Utilisateur employe : employes) {
            boolean alreadyAssigned = employe.getDocuments().stream()
                    .anyMatch(doc -> doc.getId() != null && doc.getId().equals(saved.getId()));

            if (!alreadyAssigned) {
                employe.getDocuments().add(saved);
                utilisateurRepository.save(employe);
                assignedCount++;
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "documentId", saved.getId(),
                "assignedEmployes", assignedCount
        ));
    }

    @PostMapping(value = "/send-to-demandeur/{eventId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> sendToDemandeur(
            @PathVariable Long eventId,
            @RequestPart("file") MultipartFile file,
            @RequestParam("titre") String titre,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "niveauAcces", required = false) String niveauAcces
    ) {
        Evenement ev = evenmentRepository.findById(eventId).orElseThrow();
        Utilisateur demandeur = ev.getUtilisateur();
        if (demandeur == null) throw new RuntimeException("Event has no demandeur");

        String filename = storage.store(file);

        Document d = new Document();
        d.setTitre(titre);
        d.setChemin(filename);
        d.setNiveauAcces(niveauAcces);

        String finalType = (type != null && !type.isBlank()) ? type : file.getContentType();
        d.setType(finalType);

        d = documentRepository.save(d);

        // avoid duplicate
        if (!demandeur.getDocuments().contains(d)) {
            demandeur.getDocuments().add(d);
            utilisateurRepository.save(demandeur);
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}