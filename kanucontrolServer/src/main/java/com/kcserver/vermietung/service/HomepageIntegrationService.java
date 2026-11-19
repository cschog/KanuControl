package com.kcserver.vermietung.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.vermietung.dto.*;
import com.kcserver.vermietung.dto.HomepageIntegrationDTO.*;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.Buchungsquelle;
import com.kcserver.vermietung.repository.*;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.util.*;

@Service @Transactional @RequiredArgsConstructor
public class HomepageIntegrationService {
    private final HomepageAnbindungRepository anbindungen;
    private final HomepageAnfrageRepository anfragen;
    private final BuchungRepository buchungen;
    private final BuchungService buchungService;
    private final MieterService mieterService;
    private final ObjectMapper mapper;
    private final Validator validator;
    private final com.kcserver.core.repository.VereinRepository veranstalterRepository;

    private HomepageAnbindung zugang(Long id, String clientId, boolean lock) {
        HomepageAnbindung config = (lock ? anbindungen.lockById(id) : anbindungen.findById(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Anbindung nicht freigegeben."));
        if (!config.isAktiv() || clientId == null || !clientId.equals(config.getTechnischerClientId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Anbindung nicht freigegeben.");
        }
        Mietobjekt objekt = config.getMietobjekt();
        if (!objekt.isMietbar() || !objekt.isDirektbuchungAktiv()
                || objekt.getAnreisezeit() == null || objekt.getAbreisezeit() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Das Mietobjekt ist online nicht buchbar.");
        }
        return config;
    }

    @Transactional(readOnly = true)
    public Katalog katalog(Long id, String clientId) {
        HomepageAnbindung config = zugang(id, clientId, false);
        Mietobjekt objekt = config.getMietobjekt();
        List<Bereich> bereiche = objekt.getMietbereiche().stream()
                .filter(b -> config.getErlaubteMietbereichIds().contains(b.getId()) && b.isMietbar() && b.isDirektbuchungAktiv())
                .sorted(Comparator.comparing(Mietbereich::getSortierung).thenComparing(Mietbereich::getId))
                .map(b -> new Bereich(b.getId(), b.getBezeichnung(), b.getBeschreibung(), b.getBestand(),
                        b.getMengeneinheit(), b.getBerechnungsart().name(),
                        buchungService.getPreis(b.getId(), Buchungsquelle.DIREKT, LocalDate.now(ZoneId.of("Europe/Berlin"))),
                        b.getVoraussetzungenDirekt().stream().map(Mietbereich::getId).sorted().toList()))
                .toList();
        return new Katalog(config.getVersion(), config.getBezeichnung(), objekt.getBezeichnung(),
                objekt.getAnreisezeit().toString(), objekt.getAbreisezeit().toString(),
                config.getDatenschutzUrl(), config.getHausregelnUrl(), config.isTeilnehmerPflicht(), bereiche);
    }

    @Transactional(readOnly = true)
    public Verfuegbarkeit verfuegbarkeit(Long id, String clientId, Auswahl auswahl) {
        HomepageAnbindung config = zugang(id, clientId, false);
        validiere(auswahl);
        pruefeAuswahl(config, auswahl);
        try {
            buchungService.pruefeHomepageVerfuegbarkeit(id, auswahl.anreise(), auswahl.abreise(), positionen(auswahl));
            return new Verfuegbarkeit(true, "Der gewünschte Zeitraum ist derzeit verfügbar.");
        } catch (IllegalArgumentException ex) {
            return new Verfuegbarkeit(false, ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Kalender kalender(Long id, String clientId, KalenderAuswahl dto) {
        HomepageAnbindung config = zugang(id, clientId, false);
        validiere(dto);
        YearMonth monat;
        try { monat = YearMonth.parse(dto.monat()); }
        catch (java.time.format.DateTimeParseException ex) { throw new IllegalArgumentException("Bitte einen gültigen Monat wählen."); }
        if (monat.getYear() < 2000 || monat.getYear() > 2100) throw new IllegalArgumentException("Monat außerhalb des Kalenderbereichs.");
        Mietbereich bereich = config.getMietobjekt().getMietbereiche().stream()
                .filter(b -> Objects.equals(b.getId(), dto.mietbereichId()) && b.isMietbar() && b.isDirektbuchungAktiv()
                    && config.getErlaubteMietbereichIds().contains(b.getId()))
                .findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Mietbereich nicht freigegeben."));
        return buchungService.homepageKalender(config.getMietobjekt(), bereich, monat, config.getErlaubteMietbereichIds());
    }

    public Bestaetigung anfrage(Long id, String clientId, Anfrage dto) {
        // Die Sperre serialisiert Wiederholungen für dieses Objekt. Buchung und Replay-Datensatz
        // werden in derselben Transaktion geschrieben; Netzwerk-Wiederholungen sind dadurch sicher.
        HomepageAnbindung config = zugang(id, clientId, true);
        validiere(dto);
        String hash = hash(json(dto));
        Optional<HomepageAnfrage> vorher = anfragen.findByMietobjektIdAndAnfrageId(id, dto.anfrageId().toString());
        if (vorher.isPresent()) {
            if (!vorher.get().getPayloadHash().equals(hash)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Diese Anfrage-ID wurde bereits mit anderen Angaben verwendet.");
            }
            try { return mapper.readValue(vorher.get().getAntwortJson(), Bestaetigung.class); }
            catch (JsonProcessingException ex) { throw new IllegalStateException("Gespeicherte Antwort konnte nicht gelesen werden.", ex); }
        }
        if (!Objects.equals(config.getVersion(), dto.konfigurationVersion())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Die Formularhinweise wurden geändert. Bitte das Formular neu laden und die aktuellen Hinweise prüfen.");
        }
        pruefeAuswahl(config, dto.auswahl());
        BuchungDTO buchung = new BuchungDTO();
        buchung.setAnzahlJugendliche(dto.anzahlJugendliche());
        buchung.setJugendAlterVon(dto.jugendAlterVon()); buchung.setJugendAlterBis(dto.jugendAlterBis());
        buchung.setAnzahlErwachsene(dto.anzahlErwachsene());
        if (!buchung.isTeilnehmerdatenGueltig()
                || config.isTeilnehmerPflicht() && (dto.anzahlJugendliche() == null || dto.anzahlErwachsene() == null)
                || dto.anzahlJugendliche() != null && dto.anzahlErwachsene() != null
                    && (long) dto.anzahlJugendliche() + dto.anzahlErwachsene() < 1) {
            throw new IllegalArgumentException("Bitte Teilnehmerzahlen und Altersspanne vollständig und korrekt angeben.");
        }
        // Vor der ersten Verfügbarkeitsprüfung sperren: keine veraltete Lazy-Collection
        // zwischen Vorprüfung und Anlegen der Anfrage im selben Persistence Context.
        buchungService.sperreMietobjekt(id);
        buchungService.pruefeHomepageVerfuegbarkeit(id, dto.auswahl().anreise(), dto.auswahl().abreise(), positionen(dto.auswahl()));
        // Neue, nicht öffentlich verknüpfte Person: keine ungeprüfte Überschreibung bestehender Kontakte.
        MieterSaveDTO kontakt = new MieterSaveDTO();
        kontakt.setVorname(dto.vorname()); kontakt.setName(dto.name()); kontakt.setEmail(dto.email());
        kontakt.setTelefon(dto.telefon()); kontakt.setStrasse(dto.strasse()); kontakt.setPlz(dto.plz());
        kontakt.setOrt(dto.ort()); kontakt.setCountryCode(dto.countryCode());
        if (dto.veranstalterDaten() == null) {
            kontakt.setBankName(dto.bankName()); kontakt.setIban(dto.iban()); kontakt.setBic(dto.bic());
            kontakt.setKontoinhaber(dto.kontoinhaber());
        }
        MieterDTO mieter = mieterService.create(kontakt);
        buchung.setMieterId(mieter.getPersonId()); buchung.setMietobjektId(id);
        buchung.setBuchungsquelle(Buchungsquelle.DIREKT);
        if (dto.veranstalterDaten() != null) {
            var v = dto.veranstalterDaten();
            // Reuse only an exact full match; never overwrite existing KJFP/shared master data.
            var matches = veranstalterRepository.findAll().stream().filter(x -> x.getVermietungZusammengefuehrtIn()==null).filter(x ->
                Objects.equals(x.getName(),v.name()) && Objects.equals(x.getStrasse(),v.strasse())
                && Objects.equals(x.getPlz(),v.plz()) && Objects.equals(x.getOrt(),v.ort())
                && x.getCountryCode().name().equals(v.countryCode()) && Objects.equals(x.getIban(),v.iban())
                && Objects.equals(x.getBankName(),v.bankName()) && Objects.equals(x.getBic(),v.bic())).toList();
            if (matches.size()>1) throw new ResponseStatusException(HttpStatus.CONFLICT,"Mehrere passende Veranstalter vorhanden. Bitte die Vermietung kontaktieren.");
            com.kcserver.core.entity.Verein organisation;
            if (matches.size()==1) organisation=matches.getFirst();
            else {
                organisation=new com.kcserver.core.entity.Verein(); organisation.setKjfpVerein(false);
                organisation.setName(v.name()); organisation.setAbk("HP"+UUID.randomUUID().toString().substring(0,8));
                organisation.setStrasse(v.strasse()); organisation.setPlz(v.plz()); organisation.setOrt(v.ort());
                organisation.setCountryCode(com.kcserver.kjfp.enumtype.CountryCode.valueOf(v.countryCode()));
                organisation.setBankName(v.bankName()); organisation.setIban(v.iban()); organisation.setBic(v.bic());
                organisation=veranstalterRepository.save(organisation);
            }
            buchung.setVeranstalterVereinId(organisation.getId());
        }
        buchung.setAnreise(dto.auswahl().anreise().atTime(config.getMietobjekt().getAnreisezeit()));
        buchung.setAbreise(dto.auswahl().abreise().atTime(config.getMietobjekt().getAbreisezeit()));
        buchung.setPositionen(positionen(dto.auswahl()));
        buchung.setBemerkung("Homepage-Anfrage" + (dto.veranstalter() == null || dto.veranstalter().isBlank() ? "" :
                "\nVeranstalter (ungeprüfte Angabe): " + dto.veranstalter().trim())
                + (dto.bemerkung() == null || dto.bemerkung().isBlank() ? "" : "\n" + dto.bemerkung().trim()));
        BuchungDTO saved = buchungService.create(buchung);
        Buchung entity = buchungen.findById(saved.getId()).orElseThrow();
        entity.setHerkunft("HOMEPAGE");
        Bestaetigung antwort = new Bestaetigung(saved.getId(), saved.getBuchungsnummer(), "RESERVIERT",
                true, config.isVerwaltungsBenachrichtigung(), config.getEmpfaengerEmail(),
                saved.getPositionen() == null ? "" : saved.getPositionen().stream()
                    .map(p -> p.getAnzahl() + " × " + p.getMietbereichBezeichnung())
                    .collect(java.util.stream.Collectors.joining(", ")), config.getMietobjekt().getBezeichnung());
        HomepageAnfrage protokoll = new HomepageAnfrage();
        protokoll.setMietobjektId(id); protokoll.setAnfrageId(dto.anfrageId().toString());
        protokoll.setPayloadHash(hash); protokoll.setAntwortJson(json(antwort)); protokoll.setCreatedAt(Instant.now());
        protokoll.setHinweisStand("Version " + config.getVersion() + "\nHausregeln: " + config.getHausregelnUrl()
                + "\nDatenschutz: " + config.getDatenschutzUrl() + "\nAkzeptiert bzw. zur Kenntnis genommen beim Absenden.");
        anfragen.saveAndFlush(protokoll);
        return antwort;
    }

    private void pruefeAuswahl(HomepageAnbindung config, Auswahl auswahl) {
        for (Position position : auswahl.positionen()) {
            if (!config.getErlaubteMietbereichIds().contains(position.mietbereichId())) {
                throw new IllegalArgumentException("Ein ausgewählter Mietbereich ist für diese Homepage nicht freigegeben.");
            }
        }
    }
    private List<BuchungMietbereichDTO> positionen(Auswahl auswahl) {
        return auswahl.positionen().stream().map(p -> {
            BuchungMietbereichDTO dto = new BuchungMietbereichDTO();
            dto.setMietbereichId(p.mietbereichId()); dto.setAnzahl(p.anzahl()); return dto;
        }).toList();
    }
    private void validiere(Object dto) {
        if (dto == null || !validator.validate(dto).isEmpty()) {
            throw new IllegalArgumentException("Bitte alle Pflichtfelder und Hinweise vollständig ausfüllen.");
        }
    }
    private String json(Object value) {
        try { return mapper.writeValueAsString(value); }
        catch (JsonProcessingException ex) { throw new IllegalStateException("Anfrage konnte nicht verarbeitet werden.", ex); }
    }
    private String hash(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (java.security.NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
    }
}
