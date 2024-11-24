package com.kcserver.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MitgliedDTO {
    private Long id;             // ID of the Mitglied
    private Long personId;       // ID of the associated Person
    private Long vereinId;       // ID of the associated Verein
    private String funktion;     // Role of the Mitglied
    private Boolean hauptVerein; // Indicates if this is the main Verein
}
