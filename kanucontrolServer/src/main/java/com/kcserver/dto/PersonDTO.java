package com.kcserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonDTO {
    private Long id; // Matches 'id' in Person

    @NotBlank(message = "Name must not be blank")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name; // Matches 'name' in Person

    @NotBlank(message = "Vorname must not be blank")
    @Size(min = 2, max = 100)
    private String vorname; // Matches 'vorname' in Person
    private String strasse; // Matches 'strasse' in Person
    private String plz; // Matches 'plz' in Person
    private String ort; // Matches 'ort' in Person


    @Pattern(
            regexp = "^[0-9+\\-/ ]*$",
            message = "Telefon contains invalid characters"
    )
    private String telefon; // Matches 'telefon' in Person
    private String bankName; // Matches 'bankName' in Person
    private String iban; // Matches 'iban' in Person
    private String bic; // Matches 'bic' in Person

    @Getter
    @Setter
    private List<MitgliedDTO> mitgliedschaften; // New field for memberships

}