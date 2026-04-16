package cnstn.system_de_reservation_cnstn.controllers;

import cnstn.system_de_reservation_cnstn.dto.auth.UtilisateurDto;
import cnstn.system_de_reservation_cnstn.services.UserDirectoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserDirectoryController {

    private final UserDirectoryService userDirectoryService;

    @GetMapping("/list")
    public List<UtilisateurDto> listAll() {
        return userDirectoryService.listAll();
    }
}