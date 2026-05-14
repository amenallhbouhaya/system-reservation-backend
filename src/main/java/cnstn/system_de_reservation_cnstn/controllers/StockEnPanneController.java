package cnstn.system_de_reservation_cnstn.controllers;

import cnstn.system_de_reservation_cnstn.models.StockEnPanne;
import cnstn.system_de_reservation_cnstn.services.StockEnPanneService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.CrossOrigin;
@RestController
@RequestMapping("/api/stock-en-panne")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StockEnPanneController {

    private final StockEnPanneService stockEnPanneService;

    @GetMapping
    public List<StockEnPanne> all() {
        return stockEnPanneService.all();
    }

    @PostMapping("/{id}/restore")
    public Map<String, Object> restore(Authentication auth, @PathVariable Long id) {
        return stockEnPanneService.restore(auth, id);
    }
}
