package com.kcserver.fahrten.dto;

import com.kcserver.fahrten.enumtype.KanuTyp;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public final class FahrtenDTOs {
    private FahrtenDTOs() {}

    public record KanuInput(@NotBlank String kennzeichnung, @NotBlank String bezeichnung,
                            @NotNull KanuTyp typ, @Min(1) @Max(4) int platzanzahl,
                            boolean aktiv, Long eigentuemerId) {}
    public record KanuInfo(Long id, String kennzeichnung, String bezeichnung, KanuTyp typ,
                           int platzanzahl, boolean aktiv, boolean privat, Long veranstaltungId,
                           Long eigentuemerId, String eigentuemer) {}
    public record GewaesserInput(@NotBlank String name) {}
    public record GewaesserInfo(Long id, String name) {}
    public record FahrtInput(@NotNull LocalDate datum, LocalTime beginnZeit, LocalTime endeZeit,
                             @NotNull Long gewaesserId, String startort, String zielort,
                             @NotNull @DecimalMin("0.0") BigDecimal kilometer,
                             String streckenangabe, String bemerkung) {}
    public record BootInput(@NotNull Long kanuId, boolean lizenzErforderlich) {}
    public record BesatzungInput(@NotNull Long teilnehmerId) {}
    public record LizenzInput(@NotBlank String lizenznummer, @NotNull LocalDate gueltigVon,
                              @NotNull LocalDate gueltigBis, @DecimalMin("0.0") BigDecimal kosten) {}
    public record LizenzInfo(Long id, Long kanuId, String lizenznummer, LocalDate gueltigVon,
                             LocalDate gueltigBis, BigDecimal kosten) {}
    public record CrewInfo(Long id, Long teilnehmerId, Long personId, String vorname, String name) {}
    public record BootInfo(Long fahrtKanuId, KanuInfo kanu, boolean lizenzErforderlich,
                           boolean lizenzVorhanden, List<CrewInfo> besatzung) {}
    public record FahrtInfo(Long id, Long veranstaltungId, LocalDate datum, LocalTime beginnZeit,
                            LocalTime endeZeit, GewaesserInfo gewaesser, String startort,
                            String zielort, BigDecimal kilometer, String streckenangabe,
                            String bemerkung, List<BootInfo> kanus) {}
    public record KilometerInfo(Long personId, String vorname, String name, String gewaesser,
                                BigDecimal kilometer) {}
}
