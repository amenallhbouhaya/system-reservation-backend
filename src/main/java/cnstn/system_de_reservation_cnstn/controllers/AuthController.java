package cnstn.system_de_reservation_cnstn.controllers;

import cnstn.system_de_reservation_cnstn.dto.auth.*;
import cnstn.system_de_reservation_cnstn.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<java.util.Map<String, String>> register(@RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(req));
    }

    @PostMapping("/register/verify")
    public ResponseEntity<java.util.Map<String, String>> verifyRegisterCode(@RequestBody VerifyRegisterCodeRequest req) {
        return ResponseEntity.ok(authService.verifyRegisterCode(req));
    }

    @PostMapping("/register/resend")
    public ResponseEntity<java.util.Map<String, String>> resendRegisterCode(@RequestBody ResendRegisterCodeRequest req) {
        return ResponseEntity.ok(authService.resendRegisterCode(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }
}