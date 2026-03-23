package com.kcserver.support.builder;

import com.kcserver.entity.Veranstaltung;
import com.kcserver.entity.Verein;
import com.kcserver.entity.Person;
import com.kcserver.enumtype.VeranstaltungTyp;

import java.time.LocalDate;
import java.time.LocalTime;

public class VeranstaltungBuilder {

    private String name = "Test Veranstaltung";
    private boolean aktiv = true;
    private LocalDate beginnDatum = LocalDate.now();
    private LocalDate endeDatum = LocalDate.now().plusDays(5);
    private LocalTime beginnZeit = LocalTime.now();
    private LocalTime endeZeit = LocalTime.now();
    private VeranstaltungTyp typ = VeranstaltungTyp.JEM;

    private Verein verein;
    private Person leiter;

    public static VeranstaltungBuilder aVeranstaltung() {
        return new VeranstaltungBuilder();
    }

    public VeranstaltungBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public VeranstaltungBuilder inactive() {
        this.aktiv = false;
        return this;
    }

    public VeranstaltungBuilder active() {
        this.aktiv = true;
        return this;
    }

    public VeranstaltungBuilder withVerein(Verein verein) {
        this.verein = verein;
        return this;
    }

    public VeranstaltungBuilder withLeiter(Person leiter) {
        this.leiter = leiter;
        return this;
    }

    public VeranstaltungBuilder withBeginnDatum(LocalDate beginnDatum) {
        this.beginnDatum = beginnDatum;
        return this;
    }

    public VeranstaltungBuilder withEndeDatum(LocalDate endeDatum) {
        this.endeDatum = endeDatum;
        return this;
    }

    public VeranstaltungBuilder withBeginnZeit(LocalTime beginnZeit) {
        this.beginnZeit = beginnZeit;
        return this;
    }

    public VeranstaltungBuilder withAktiv(boolean aktiv) {
        this.aktiv = aktiv;
        return this;
    }

    public VeranstaltungBuilder withEndeZeit(LocalTime endeZeit) {
        this.endeZeit = endeZeit;
        return this;
    }

    public VeranstaltungBuilder withTyp(VeranstaltungTyp typ) {
        this.typ = typ;
        return this;
    }

    public Veranstaltung build() {

        if (verein == null) {
            throw new IllegalStateException("Verein muss gesetzt sein");
        }

        if (leiter == null) {
            throw new IllegalStateException("Leiter muss gesetzt sein");
        }

        if (beginnDatum == null || endeDatum == null) {
            throw new IllegalStateException("Beginn/Ende Datum fehlt");
        }

        Veranstaltung v = new Veranstaltung();
        v.setName(name);
        v.setAktiv(aktiv);
        v.setBeginnDatum(beginnDatum);
        v.setEndeDatum(endeDatum);
        v.setBeginnZeit(beginnZeit);
        v.setEndeZeit(endeZeit);
        v.setTyp(typ);
        v.setVerein(verein);
        v.setLeiter(leiter);

        return v;
    }
}