// dto/finanzen/FoerderungDashboardDTO.java

package com.kcserver.dto.finanzen;

import java.math.BigDecimal;

public class FoerderungDashboardDTO {

    private Integer foerderfaehigeTeilnehmer;

    private Integer foerdertage;

    private BigDecimal foerdersatz;

    private BigDecimal gesamtfoerderung;

    public Integer getFoerderfaehigeTeilnehmer() {
        return foerderfaehigeTeilnehmer;
    }

    public void setFoerderfaehigeTeilnehmer(Integer foerderfaehigeTeilnehmer) {
        this.foerderfaehigeTeilnehmer = foerderfaehigeTeilnehmer;
    }

    public Integer getFoerdertage() {
        return foerdertage;
    }

    public void setFoerdertage(Integer foerdertage) {
        this.foerdertage = foerdertage;
    }

    public BigDecimal getFoerdersatz() {
        return foerdersatz;
    }

    public void setFoerdersatz(BigDecimal foerdersatz) {
        this.foerdersatz = foerdersatz;
    }

    public BigDecimal getGesamtfoerderung() {
        return gesamtfoerderung;
    }

    public void setGesamtfoerderung(BigDecimal gesamtfoerderung) {
        this.gesamtfoerderung = gesamtfoerderung;
    }
}