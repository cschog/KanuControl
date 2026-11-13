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
        String typ=v==null?"Mieters":"Veranstalters";
        String strasse=pflicht(v==null?p.getStrasse():v.getStrasse(),"Straße des "+typ);
        String plz=pflicht(v==null?p.getPlz():v.getPlz(),"Postleitzahl des "+typ);
        String ort=pflicht(v==null?p.getOrt():v.getOrt(),"Ort des "+typ);
        CountryCode land=v==null?p.getCountryCode():v.getCountryCode();
        return Map.of("empfaenger_name",(organisation.isBlank()?"":organisation+"\n")+person,
            "empfaenger_strasse",strasse,"empfaenger_ort",plz+" "+ort+(land==null||land==CountryCode.DE?"":"\n"+land.name()));
    }
    public static String organisation(Buchung b) {return b.getVeranstalter()==null?leer(b.getMieter().getOrganisation()):leer(b.getVeranstalter().getName());}
    private static String leer(String s){return s==null?"":s.strip().replaceAll("[\\p{Cntrl}]"," ");}
    private static String pflicht(String s,String label){if(s==null||s.isBlank())throw fehler(label+" fehlt. Bitte die Stammdaten vervollständigen.");return leer(s);}
}
