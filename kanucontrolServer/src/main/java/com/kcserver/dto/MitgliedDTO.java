package com.kcserver.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Mitglied entity with essential attributes.
 */
@Data
@NoArgsConstructor
public class MitgliedDTO {

    private Long id;                  // ID of the Mitglied record
    private Long personId;            // ID of the associated Person
    private Long vereinId;            // ID of the associated Verein
    private String vereinName;        // Name of the associated Verein
    private String vereinAbk;         // Abbreviation of the Verein
    private String funktion;          // Role of the person in the Verein
    private Boolean hauptVerein;      // Indicates if this is the main Verein

    // Custom Constructor
    public MitgliedDTO(Long personId, Long vereinId, String vereinName, String vereinAbk, String funktion, Boolean hauptVerein) {
        this.personId = personId;
        this.vereinId = vereinId;
        this.vereinName = vereinName;
        this.vereinAbk = vereinAbk;
        this.funktion = funktion;
        this.hauptVerein = hauptVerein;
    }
}