// dto/finanzen/FinanzenDashboardDTO.java

package com.kcserver.dto.finanzen;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class FinanzenDashboardDTO {

    private BigDecimal planKosten;

    private BigDecimal istKosten;

    private BigDecimal planEinnahmen;

    private BigDecimal istEinnahmen;

    private BigDecimal planSaldo;

    private BigDecimal istSaldo;

    private FoerderungDashboardDTO foerderung;

    private List<BetragPositionDTO> kostenNachKategorie = new ArrayList<>();

    private List<BetragPositionDTO> einnahmenNachKategorie = new ArrayList<>();

    private List<BetragPositionDTO> istKostenNachKategorie = new ArrayList<>();

    private List<BetragPositionDTO> istEinnahmenNachKategorie = new ArrayList<>();

    public List<BetragPositionDTO> getIstKostenNachKategorie() {
        return istKostenNachKategorie;
    }

    public void setIstKostenNachKategorie(
            List<BetragPositionDTO> istKostenNachKategorie
    ) {
        this.istKostenNachKategorie = istKostenNachKategorie;
    }

    public List<BetragPositionDTO> getIstEinnahmenNachKategorie() {
        return istEinnahmenNachKategorie;
    }

    public void setIstEinnahmenNachKategorie(
            List<BetragPositionDTO> istEinnahmenNachKategorie
    ) {
        this.istEinnahmenNachKategorie = istEinnahmenNachKategorie;
    }

    public BigDecimal getPlanKosten() {
        return planKosten;
    }

    public void setPlanKosten(BigDecimal planKosten) {
        this.planKosten = planKosten;
    }

    public BigDecimal getIstKosten() {
        return istKosten;
    }

    public void setIstKosten(BigDecimal istKosten) {
        this.istKosten = istKosten;
    }

    public BigDecimal getPlanEinnahmen() {
        return planEinnahmen;
    }

    public void setPlanEinnahmen(BigDecimal planEinnahmen) {
        this.planEinnahmen = planEinnahmen;
    }

    public BigDecimal getIstEinnahmen() {
        return istEinnahmen;
    }

    public void setIstEinnahmen(BigDecimal istEinnahmen) {
        this.istEinnahmen = istEinnahmen;
    }

    public BigDecimal getPlanSaldo() {
        return planSaldo;
    }

    public void setPlanSaldo(BigDecimal planSaldo) {
        this.planSaldo = planSaldo;
    }

    public BigDecimal getIstSaldo() {
        return istSaldo;
    }

    public void setIstSaldo(BigDecimal istSaldo) {
        this.istSaldo = istSaldo;
    }

    public FoerderungDashboardDTO getFoerderung() {
        return foerderung;
    }

    public void setFoerderung(FoerderungDashboardDTO foerderung) {
        this.foerderung = foerderung;
    }

    public List<BetragPositionDTO> getKostenNachKategorie() {
        return kostenNachKategorie;
    }

    public void setKostenNachKategorie(List<BetragPositionDTO> kostenNachKategorie) {
        this.kostenNachKategorie = kostenNachKategorie;
    }

    public List<BetragPositionDTO> getEinnahmenNachKategorie() {
        return einnahmenNachKategorie;
    }

    public void setEinnahmenNachKategorie(List<BetragPositionDTO> einnahmenNachKategorie) {
        this.einnahmenNachKategorie = einnahmenNachKategorie;
    }
}