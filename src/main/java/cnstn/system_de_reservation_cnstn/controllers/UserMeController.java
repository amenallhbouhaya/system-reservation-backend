package cnstn.system_de_reservation_cnstn.controllers;

import cnstn.system_de_reservation_cnstn.dto.user.ChangePasswordRequest;
import cnstn.system_de_reservation_cnstn.dto.user.MeResponse;
import cnstn.system_de_reservation_cnstn.dto.user.UpdateMeRequest;
import cnstn.system_de_reservation_cnstn.services.UserMeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.CrossOrigin;
@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserMeController {

    private final UserMeService userMeService;

    @GetMapping
    public MeResponse me(Authentication auth) {
        return userMeService.me(auth.getName());
    }

    @PutMapping
    public MeResponse updateMe(Authentication auth, @RequestBody UpdateMeRequest req) {
        return userMeService.updateMe(auth.getName(), req);
    }

    @PostMapping("/password")
    public void changePassword(Authentication auth, @RequestBody ChangePasswordRequest req) {
        userMeService.changePassword(auth.getName(), req);
    }

    @PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadPhoto(Authentication auth,
                                            @RequestPart("file") MultipartFile file) throws Exception {
        userMeService.uploadPhoto(auth.getName(), file);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/photo")
    public ResponseEntity<byte[]> getPhoto(Authentication auth) {
        return userMeService.getPhoto(auth.getName());
    }
}