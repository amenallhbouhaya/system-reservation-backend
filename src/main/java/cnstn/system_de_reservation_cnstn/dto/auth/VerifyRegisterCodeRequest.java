package cnstn.system_de_reservation_cnstn.dto.auth;

public record VerifyRegisterCodeRequest(
        String email,
        String code
) {
}
