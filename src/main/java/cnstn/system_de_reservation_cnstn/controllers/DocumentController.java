package cnstn.system_de_reservation_cnstn.controllers;

import cnstn.system_de_reservation_cnstn.dto.DocumentItemDto;
import cnstn.system_de_reservation_cnstn.models.Document;
import cnstn.system_de_reservation_cnstn.models.Utilisateur;
import cnstn.system_de_reservation_cnstn.repository.DocumentRepository;
import cnstn.system_de_reservation_cnstn.repository.UtilisateurRepository;
import cnstn.system_de_reservation_cnstn.services.DocumentService;
import cnstn.system_de_reservation_cnstn.services.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.core.io.Resource;
@RestController
@RequestMapping("/Document")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final UtilisateurRepository utilisateurRepository;
    private final DocumentRepository documentRepository;
    private final FileStorageService storage;
    @PostMapping("/add")
    public Document createDocument(@RequestBody Document document) {
        return documentService.CreateDocument(document);
    }

    @GetMapping("/all")
    public List<Document> afficher() {
        return documentService.findAll();
    }

    @PutMapping("/{id}")
    public Document updateDocument(@PathVariable Long id, @RequestBody Document document) {
        return documentService.updateDocument(id, document);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable Long id) {
        documentService.deleteById(id);
        return ResponseEntity.ok("Document supprimé avec succès");
    }

    // ✅ Documents متاع المستخدم الحالي (many-to-many)
    @GetMapping("/me")
    public List<DocumentItemDto> myDocs(Authentication auth) {
        Utilisateur u = utilisateurRepository.findByEmail(auth.getName()).orElseThrow();

        return u.getDocuments().stream()
                .map(d -> new DocumentItemDto(
                        d.getId(),
                        d.getTitre(),
                        d.getType(),
                        d.getChemin(),
                        d.getNiveauAcces()
                ))
                .toList();
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        Document d = documentRepository.findById(id).orElseThrow();
        Resource res = storage.loadAsResource(d.getChemin());

        // content-type إذا موجود
        MediaType mt = MediaType.APPLICATION_OCTET_STREAM;
        if (d.getType() != null && !d.getType().isBlank()) {
            try { mt = MediaType.parseMediaType(d.getType()); } catch (Exception ignored) {}
        }

        // اسم ملف nicer (ينحي UUID_)
        String filename = d.getChemin();
        int idx = filename.indexOf("_");
        if (idx > 0 && idx < filename.length() - 1) filename = filename.substring(idx + 1);

        return ResponseEntity.ok()
                .contentType(mt)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(res);
    }
}