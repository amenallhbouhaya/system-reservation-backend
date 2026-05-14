package cnstn.system_de_reservation_cnstn.controllers;

import cnstn.system_de_reservation_cnstn.dto.DocumentItemDto;
import cnstn.system_de_reservation_cnstn.models.Document;
import cnstn.system_de_reservation_cnstn.services.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.web.bind.annotation.CrossOrigin;
@RestController
@RequestMapping({"/api/documents", "/Document"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping({"", "/add"})
    public Document createDocument(@RequestBody Document document) {
        return documentService.CreateDocument(document);
    }

    @GetMapping({"", "/all"})
    public List<Document> afficher() {
        return documentService.findAll();
    }

    @PutMapping("/{id}")
    public Document updateDocument(@PathVariable Long id, @RequestBody Document document) {
        return documentService.updateDocument(id, document);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        documentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ Documents متاع المستخدم الحالي (many-to-many)
    @GetMapping("/me")
    public List<DocumentItemDto> myDocs(Authentication auth) {
        return documentService.myDocs(auth.getName());
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        return documentService.download(id);
    }
}