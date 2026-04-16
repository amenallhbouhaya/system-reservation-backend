package cnstn.system_de_reservation_cnstn.controllers;

import cnstn.system_de_reservation_cnstn.services.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping({"/api/directeur-dsn/documents", "/directeur-dsn/documents"})
@RequiredArgsConstructor
public class DirecteurDsnDocumentController {

    private final DocumentService documentService;

    @PostMapping(value = {"/send-to-employes", "/send-to-employees"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> sendToEmployes(
            @RequestPart("file") MultipartFile file,
            @RequestParam("titre") String titre,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "niveauAcces", required = false) String niveauAcces
    ) {
        return documentService.sendToEmployes(file, titre, type, niveauAcces);
    }

    @PostMapping(value = "/send-to-demandeur/{eventId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> sendToDemandeur(
            @PathVariable Long eventId,
            @RequestPart("file") MultipartFile file,
            @RequestParam("titre") String titre,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "niveauAcces", required = false) String niveauAcces
    ) {
        return documentService.sendToDemandeur(eventId, file, titre, type, niveauAcces);
    }
}