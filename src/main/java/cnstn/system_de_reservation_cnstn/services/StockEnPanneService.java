package cnstn.system_de_reservation_cnstn.services;

import cnstn.system_de_reservation_cnstn.models.Equipement;
import cnstn.system_de_reservation_cnstn.models.StockEnPanne;
import cnstn.system_de_reservation_cnstn.repository.StockEnPanneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StockEnPanneService {

    private final StockEnPanneRepository stockEnPanneRepository;
    private final EquipementService equipementService;

    public List<StockEnPanne> all() {
        return stockEnPanneRepository.findAll();
    }

    @Transactional
    public Map<String, Object> restore(Authentication auth, Long id) {
        assertAdmin(auth);

        StockEnPanne item = stockEnPanneRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Piece introuvable"));

        String[] parsed = splitPieceLabel(item.getNomPiece());
        Equipement equipement = new Equipement();
        equipement.setNom(parsed[0]);
        equipement.setNumeroSerie(parsed[1]);

        Equipement restored = equipementService.createEquipement(equipement);
        stockEnPanneRepository.delete(item);

        return Map.of(
                "status", "restored",
                "restoredEquipementId", restored.getId(),
                "restoredNom", restored.getNom()
        );
    }

    private static String normalizeRole(String value) {
        if (value == null) return "";
        return value.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }

    private boolean hasRole(Authentication auth, String expectedNormalizedRole) {
        if (auth == null || auth.getAuthorities() == null) return false;

        return auth.getAuthorities().stream().anyMatch(a -> {
            String role = a.getAuthority();
            if (role != null && role.startsWith("ROLE_")) {
                role = role.substring(5);
            }
            return normalizeRole(role).equals(expectedNormalizedRole);
        });
    }

    private void assertAdmin(Authentication auth) {
        if (hasRole(auth, "admin")) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acces reserve a l'admin");
    }

    private String[] splitPieceLabel(String rawLabel) {
        String label = rawLabel == null ? "" : rawLabel.trim();
        if (label.isBlank()) {
            return new String[]{"Equipement restaure", null};
        }

        String[] parts = label.split("\\s+-\\s+", 2);
        String nom = parts[0].trim();
        if (nom.isBlank()) {
            nom = "Equipement restaure";
        }

        String numeroSerie = null;
        if (parts.length > 1) {
            numeroSerie = parts[1] == null ? null : parts[1].trim();
            if (numeroSerie != null && numeroSerie.isBlank()) {
                numeroSerie = null;
            }
        }

        return new String[]{nom, numeroSerie};
    }
}
