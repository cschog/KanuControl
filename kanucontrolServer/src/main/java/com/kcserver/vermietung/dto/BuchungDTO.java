package com.kcserver.vermietung.dto;

import com.kcserver.vermietung.enumtype.Buchungsstatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BuchungDTO {

    private Long id;
    private String buchungsnummer;
    private Buchungsstatus status;

    @NotNull
    private LocalDate anreise;

    @NotNull
    private LocalDate abreise;

    @NotNull
    private Long mietobjektId;

    private List<Long> mietbereichIds;

    private List<MietbereichRefDTO> mietbereiche;

    @NotNull
    private Long mieterId;

    private Long veranstalterVereinId;

    // Anzeigeinformationen
    private String mieterVorname;
    private String mieterName;
    private String mietobjektBezeichnung;
}