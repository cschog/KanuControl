package com.kcserver.dto.erhebungsbogen;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ErhebungsbogenDTO {

    private Long id;

    /* =========================
       Bezug
       ========================= */

    private Long veranstaltungId;

    /* =========================
       Status
       ========================= */

    private boolean abgeschlossen;
    private LocalDate stichtag;

    /* =========================
       Statistik – Teilnehmer
       ========================= */

    private Integer teilnehmerMaennlichU6;
    private Integer teilnehmerMaennlich6_13;
    private Integer teilnehmerMaennlich14_17;
    private Integer teilnehmerMaennlich18Plus;

    private Integer teilnehmerWeiblichU6;
    private Integer teilnehmerWeiblich6_13;
    private Integer teilnehmerWeiblich14_17;
    private Integer teilnehmerWeiblich18Plus;

    private Integer teilnehmerDiversU6;
    private Integer teilnehmerDivers6_13;
    private Integer teilnehmerDivers14_17;
    private Integer teilnehmerDivers18Plus;

    /* =========================
       Statistik – Mitarbeitende
       ========================= */

    private Integer mitarbeiterMaennlich;
    private Integer mitarbeiterWeiblich;
    private Integer mitarbeiterDivers;

    /* =========================
       Optional
       ========================= */

    private String bemerkung;
}