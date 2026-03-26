package cnstn.system_de_reservation_cnstn.controllers;

import cnstn.system_de_reservation_cnstn.models.StockEnPanne;
import cnstn.system_de_reservation_cnstn.repository.StockEnPanneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stock-en-panne")
@RequiredArgsConstructor
public class StockEnPanneController {

    private final StockEnPanneRepository stockEnPanneRepository;

    @GetMapping
    public List<StockEnPanne> all() {
        return stockEnPanneRepository.findAll();
    }
}
