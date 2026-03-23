package com.kcserver.support.builder;

import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Person;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.enumtype.TeilnehmerRolle;

public class TeilnehmerBuilder {

    private Person person;
    private Veranstaltung veranstaltung;
    private TeilnehmerRolle rolle;

    public static TeilnehmerBuilder aTeilnehmer() {
        return new TeilnehmerBuilder();
    }

    public TeilnehmerBuilder withPerson(Person person) {
        this.person = person;
        return this;
    }

    public TeilnehmerBuilder withVeranstaltung(Veranstaltung v) {
        this.veranstaltung = v;
        return this;
    }

    public TeilnehmerBuilder asLeiter() {
        this.rolle = TeilnehmerRolle.LEITER;
        return this;
    }

    public Teilnehmer build() {
        Teilnehmer t = new Teilnehmer();
        t.setPerson(person);
        t.setVeranstaltung(veranstaltung);
        t.setRolle(rolle);
        return t;
    }
}