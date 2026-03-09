package cnstn.system_de_reservation_cnstn.dto;

import java.util.Date;

public record NotificationDto(
        Long id,
        String message,
        String type,
        String targetPath,
        boolean lu,
        Date dateCreation
) {
}
