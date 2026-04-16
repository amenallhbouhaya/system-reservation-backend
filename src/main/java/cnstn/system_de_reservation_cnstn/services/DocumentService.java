package cnstn.system_de_reservation_cnstn.services;

import cnstn.system_de_reservation_cnstn.dto.DocumentItemDto;
import cnstn.system_de_reservation_cnstn.models.Document;
import cnstn.system_de_reservation_cnstn.models.Evenement;
import cnstn.system_de_reservation_cnstn.models.Utilisateur;
import cnstn.system_de_reservation_cnstn.repository.DocumentRepository;
import cnstn.system_de_reservation_cnstn.repository.EvenmentRepository;
import cnstn.system_de_reservation_cnstn.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EvenmentRepository evenmentRepository;
    private final FileStorageService storage;

    public Document CreateDocument(Document document) {
        return documentRepository.save(document
        );
    }

    public List<Document> findAll() {
        return documentRepository.findAll();
    }

    public Document updateDocument(Long id, Document document) {
        Document existingDocument = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));
        existingDocument.setTitre(document.getTitre());
        existingDocument.setType(document.getType());
        existingDocument.setChemin(document.getChemin());
        existingDocument.setNiveauAcces(document.getNiveauAcces());
        return documentRepository.save(existingDocument);
    }

    public void deleteById(Long id) {
        documentRepository.deleteById(id);
    }

    public List<DocumentItemDto> myDocs(String email) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email).orElseThrow();
        return utilisateur.getDocuments().stream()
                .map(d -> new DocumentItemDto(
                        d.getId(),
                        d.getTitre(),
                        d.getType(),
                        d.getChemin(),
                        d.getNiveauAcces()
                ))
                .toList();
    }

    public ResponseEntity<Resource> download(Long id) {
        Document document = documentRepository.findById(id).orElseThrow();
        Resource resource = storage.loadAsResource(document.getChemin());

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (document.getType() != null && !document.getType().isBlank()) {
            try {
                mediaType = MediaType.parseMediaType(document.getType());
            } catch (Exception ignored) {
            }
        }

        String filename = document.getChemin();
        int idx = filename.indexOf("_");
        if (idx > 0 && idx < filename.length() - 1) {
            filename = filename.substring(idx + 1);
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    public ResponseEntity<Map<String, Object>> sendToEmployes(
            MultipartFile file,
            String titre,
            String type,
            String niveauAcces
    ) {
        String filename = storage.store(file);

        Document document = new Document();
        document.setTitre(titre);
        document.setChemin(filename);
        document.setNiveauAcces(niveauAcces);

        String finalType = (type != null && !type.isBlank()) ? type : file.getContentType();
        document.setType(finalType);

        Document saved = documentRepository.save(document);

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

    public ResponseEntity<Void> sendToDemandeur(
            Long eventId,
            MultipartFile file,
            String titre,
            String type,
            String niveauAcces
    ) {
        Evenement evenement = evenmentRepository.findById(eventId).orElseThrow();
        Utilisateur demandeur = evenement.getUtilisateur();
        if (demandeur == null) {
            throw new RuntimeException("Event has no demandeur");
        }

        String filename = storage.store(file);

        Document document = new Document();
        document.setTitre(titre);
        document.setChemin(filename);
        document.setNiveauAcces(niveauAcces);

        String finalType = (type != null && !type.isBlank()) ? type : file.getContentType();
        document.setType(finalType);

        document = documentRepository.save(document);

        if (!demandeur.getDocuments().contains(document)) {
            demandeur.getDocuments().add(document);
            utilisateurRepository.save(demandeur);
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
