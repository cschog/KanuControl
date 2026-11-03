package com.kcserver.unit;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import com.kcserver.vermietung.ical.*;
import org.junit.jupiter.api.*;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AirbnbIcalRendererTest {
    Mietobjekt objekt; Mietbereich eg,og,grill; LocalDate heute=LocalDate.of(2027,5,1);
    @BeforeEach void setup(){
        objekt=new Mietobjekt();objekt.setId(7L);objekt.setAnreisezeit(LocalTime.of(16,0));objekt.setAbreisezeit(LocalTime.of(14,0));
        eg=bereich(10L);og=bereich(11L);grill=bereich(12L);objekt.getMietbereiche().addAll(List.of(eg,og,grill));
    }
    Mietbereich bereich(long id){var b=new Mietbereich();b.setId(id);b.setMietobjekt(objekt);return b;}
    Buchung buchung(long id,Mietbereich b,String status,LocalDateTime von,LocalDateTime bis){
        var booking=new Buchung();booking.setId(id);booking.setMietobjekt(objekt);booking.setStatus(Buchungsstatus.valueOf(status));booking.setAnreise(von);booking.setAbreise(bis);
        var pos=new BuchungMietbereich();pos.setMietbereich(b);pos.setAnzahl(1);booking.getMietbereiche().add(pos);booking.setBemerkung("Geheimer Gasttext");return booking;
    }
    Buchung standard(long id,Mietbereich b,String status){return buchung(id,b,status,LocalDateTime.of(2027,5,21,16,0),LocalDateTime.of(2027,5,23,14,0));}
    String render(Buchung... items){return AirbnbIcalRenderer.render("club_a",objekt,Set.of(10L,11L),List.of(items),heute,true);}
    @Test void freitagBisSonntagBlockiertAuchDieSonntagnacht(){String ics=render(standard(1,eg,"BESTAETIGT"));assertTrue(ics.contains("DTSTART;VALUE=DATE:20270521\r\n"));assertTrue(ics.contains("DTEND;VALUE=DATE:20270524\r\n"));assertEquals(1,ics.split("BEGIN:VEVENT",-1).length-1);}
    @Test void egOderOgBlockierenGanzesBootshausExtrasAlleinNicht(){assertTrue(render(standard(1,eg,"RESERVIERT")).contains("BEGIN:VEVENT"));assertTrue(render(standard(2,og,"BESTAETIGT")).contains("BEGIN:VEVENT"));assertFalse(render(standard(3,grill,"BESTAETIGT")).contains("BEGIN:VEVENT"));}
    @Test void nurReserviertUndBestaetigt(){for(var status:Buchungsstatus.values()){String text=render(standard(1,eg,status.name()));assertEquals(status==Buchungsstatus.RESERVIERT||status==Buchungsstatus.BESTAETIGT,text.contains("BEGIN:VEVENT"),status.name());}}
    @Test void beideBereicheInEinerBuchungErzeugenNurEinEvent(){var b=standard(1,eg,"BESTAETIGT");var pos=new BuchungMietbereich();pos.setMietbereich(og);pos.setAnzahl(1);b.getMietbereiche().add(pos);assertEquals(1,render(b).split("BEGIN:VEVENT",-1).length-1);}
    @Test void belegungskomponentenWerdenBeruecksichtigt(){var paket=bereich(20L);paket.getBelegteMietbereiche().add(eg);assertTrue(render(standard(1,paket,"BESTAETIGT")).contains("BEGIN:VEVENT"));}
    @Test void fremdesObjektBleibtDraussen(){var b=standard(1,eg,"BESTAETIGT");var fremd=new Mietobjekt();fremd.setId(8L);b.setMietobjekt(fremd);assertFalse(render(b).contains("BEGIN:VEVENT"));}
    @Test void sperrenExportierenKeineGruendeOderIds(){var s=new MietobjektSperrzeit();s.setId(9L);s.setVon(LocalDateTime.of(2027,5,21,0,0));s.setBis(LocalDateTime.of(2027,5,22,0,0));s.setGrund("Geheime Reparatur");objekt.getSperrzeiten().add(s);String ics=render();assertTrue(ics.contains("SUMMARY:Gesperrt"));assertTrue(ics.contains("DTSTART;VALUE=DATE:20270520"));assertTrue(ics.contains("DTEND;VALUE=DATE:20270522"));assertFalse(ics.contains("Reparatur"));assertFalse(ics.contains("grund"));}
    @Test void sperreInWechselpauseBlockiertKeineNacht(){var s=new MietobjektSperrzeit();s.setId(9L);s.setVon(LocalDateTime.of(2027,5,21,14,0));s.setBis(LocalDateTime.of(2027,5,21,16,0));objekt.getSperrzeiten().add(s);assertFalse(render().contains("BEGIN:VEVENT"));}
    @Test void spaeteAbreiseBlockiertWeitereNacht(){var b=standard(1,eg,"BESTAETIGT");b.setAbreise(LocalDateTime.of(2027,5,23,18,0));assertTrue(render(b).contains("DTEND;VALUE=DATE:20270524"));}
    @Test void frueheAnreiseBlockiertVornacht(){var b=standard(1,eg,"BESTAETIGT");b.setAnreise(LocalDateTime.of(2027,5,21,12,0));assertTrue(render(b).contains("DTSTART;VALUE=DATE:20270520"));}
    @Test void dtendBleibtBeiSommerzeitwechselDatum(){var b=buchung(1,eg,"BESTAETIGT",LocalDateTime.of(2027,10,30,16,0),LocalDateTime.of(2027,11,1,14,0));assertTrue(render(b).contains("DTEND;VALUE=DATE:20271102"));}
    @Test void uidBleibtBeiZeitUndStatusaenderungStabilUndMandantenGetrennt(){var b=standard(1,eg,"RESERVIERT");String a=render(b).lines().filter(l->l.startsWith("UID:")).findFirst().orElseThrow();b.setStatus(Buchungsstatus.BESTAETIGT);b.setAbreise(b.getAbreise().plusDays(1));String text=render(b);assertTrue(text.contains(a));assertFalse(text.contains("Geheimer"));assertFalse(AirbnbIcalRenderer.render("club_b",objekt,Set.of(10L),List.of(b),heute,true).contains(a));}
    @Test void langzeitbelegungWirdAufZweiJahreBegrenzt(){var b=standard(1,eg,"BESTAETIGT");b.setAbreise(LocalDateTime.of(2999,12,31,14,0));assertTrue(render(b).contains("DTEND;VALUE=DATE:20290502"));}
    @Test void inaktiveUndStornierteKalenderSindGueltigLeer(){String empty=AirbnbIcalRenderer.render("club_a",objekt,Set.of(),List.of(standard(1,eg,"BESTAETIGT")),heute,false);assertTrue(empty.startsWith("BEGIN:VCALENDAR\r\n"));assertTrue(empty.endsWith("END:VCALENDAR\r\n"));assertFalse(empty.contains("VEVENT"));assertFalse(render(standard(1,eg,"STORNIERT")).contains("VEVENT"));}
    @Test void zeilenSindRfcKonformUndKuerzerAls75Bytes(){String ics=render(standard(1,eg,"BESTAETIGT"));assertFalse(ics.replace("\r\n","").contains("\n"));for(String line:ics.split("\r\n"))assertTrue(line.getBytes(java.nio.charset.StandardCharsets.UTF_8).length<=75,line);assertTrue(ics.contains("TRANSP:OPAQUE"));}
    @Test void auchReservierungSperrtDenAbreisetag(){assertTrue(render(standard(1,eg,"RESERVIERT")).contains("DTEND;VALUE=DATE:20270524"));}
    @Test void endeUmMitternachtSperrtKeinenUnbenutztenFolgetag(){var b=standard(1,eg,"BESTAETIGT");b.setAbreise(LocalDateTime.of(2027,5,23,0,0));assertTrue(render(b).contains("DTEND;VALUE=DATE:20270523"));}
    @Test void oeffentlicherHomepageNameWirdNichtInDenAirbnbExportUebernommen(){
        var b=standard(1,eg,"BESTAETIGT");var person=new com.kcserver.core.entity.Person();person.setVorname("Erika");person.setName("Musterfrau");person.setEmail("privat@example.invalid");b.setMieter(person);
        String text=render(b);assertFalse(text.contains("Erika"));assertFalse(text.contains("Musterfrau"));assertFalse(text.contains("privat@example.invalid"));assertTrue(text.contains("SUMMARY:Belegt"));
    }
}
