package cnstn.system_de_reservation_cnstn.services;

import cnstn.system_de_reservation_cnstn.dto.salle.SalleRequest;
import cnstn.system_de_reservation_cnstn.dto.salle.SalleResponse;
import cnstn.system_de_reservation_cnstn.models.Salle;
import cnstn.system_de_reservation_cnstn.repository.SalleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SalleService {

    private final SalleRepository saleRepository;

    public SalleResponse create(SalleRequest request) {
        Salle salle = new Salle();
        applyRequest(salle, request);
        return toResponse(saleRepository.save(salle));
    }

    public List<SalleResponse> findAll() {
        return saleRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public SalleResponse update(Long id, SalleRequest request) {
        Salle existingSalle = saleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Salle not found with id: " + id));
        applyRequest(existingSalle, request);
        return toResponse(saleRepository.save(existingSalle));
    }

    public void deleteById(Long id) {
        saleRepository.deleteById(id);
    }

    private void applyRequest(Salle salle, SalleRequest request) {
        salle.setNom(request.nom());
        salle.setCapacite(request.capacite());
        salle.setDescription(request.description());
    }

    private SalleResponse toResponse(Salle salle) {
        return new SalleResponse(
                salle.getId(),
                salle.getNom(),
                salle.getCapacite(),
                salle.getDescription()
        );
    }
}
