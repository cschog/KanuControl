package com.kcserver.vermietung.service;

import com.kcserver.core.util.PersonName;
import com.kcserver.vermietung.entity.Buchung;
import com.kcserver.kjfp.enumtype.CountryCode;
import java.util.Map;
import static com.kcserver.vermietung.service.VorlagenModell.fehler;

/** Document addressee only. Email routing always uses the renter separately. */
public final class Buchungsadressat {
    private Buchungsadressat() {}
    public static Map<String,String> felder(Buchung b) {
        var p=b.getMieter();
        if(p==null)throw fehler("Mieter fehlt.");
        String person=PersonName.givenFirst(pflicht(p.getName(),"Nachname des Mieters"), pflicht(p.getVorname(),"Vorname des Mieters"));
        var v=b.getVeranstalter();
        String organisation=v==null?leer(p.getOrganisation()):pflicht(v.getName(),"Veranstaltername");
        String strasse=v==null?leer(p.getStrasse()):pflicht(v.getStrasse(),"Straße des Veranstalters");
        String plz=v==null?leer(p.getPlz()):pflicht(v.getPlz(),"Postleitzahl des Veranstalters");
        String ort=v==null?leer(p.getOrt()):pflicht(v.getOrt(),"Ort des Veranstalters");
        CountryCode land=v==null?p.getCountryCode():v.getCountryCode();
        String ortszeile=(plz+" "+ort).strip();
        if(land!=null && land!=CountryCode.DE && (!strasse.isBlank() || !ortszeile.isBlank()))
            ortszeile+=(ortszeile.isBlank()?"":"\n")+land.name();
        return Map.of("empfaenger_name",(organisation.isBlank()?"":organisation+"\n")+person,
            "empfaenger_strasse",strasse,"empfaenger_ort",ortszeile);
    }
    public static String organisation(Buchung b) {return b.getVeranstalter()==null?leer(b.getMieter().getOrganisation()):leer(b.getVeranstalter().getName());}
    private static String leer(String s){return s==null?"":s.strip().replaceAll("[\\p{Cntrl}]"," ");}
    private static String pflicht(String s,String label){if(s==null||s.isBlank())throw fehler(label+" fehlt. Bitte die Stammdaten vervollständigen.");return leer(s);}
}
