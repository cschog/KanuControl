package com.kcserver.fahrten.service;

import com.kcserver.core.util.PersonName;
import com.kcserver.core.entity.Person;
import com.kcserver.fahrten.dto.FahrtenDTOs.*;
import com.kcserver.fahrten.entity.*;
import com.kcserver.core.entity.Veranstaltung;
import com.kcserver.core.entity.Teilnehmer;
import com.kcserver.fahrten.repository.*;
import com.kcserver.core.repository.VeranstaltungRepository;
import com.kcserver.core.repository.TeilnehmerRepository;
import com.kcserver.kjfp.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class FahrtenService {
    private final VeranstaltungRepository veranstaltungRepository;
    private final TeilnehmerRepository teilnehmerRepository;
    private final KanuRepository kanuRepository;
    private final GewaesserRepository gewaesserRepository;
    private final FahrtRepository fahrtRepository;
    private final FahrtKanuRepository fahrtKanuRepository;
    private final FahrtBesatzungRepository besatzungRepository;
    private final KanuLizenzRepository lizenzRepository;
    private final PersonRepository personRepository;

    public List<KanuInfo> kanus() { return kanuRepository.findByVeranstaltungIsNullOrderByKennzeichnung().stream().map(this::toKanu).toList(); }
    public List<KanuInfo> privateKanus(Long eventId) { event(eventId); return kanuRepository.findByVeranstaltung_IdOrderByKennzeichnung(eventId).stream().map(this::toKanu).toList(); }
    public KanuInfo createKanu(KanuInput input, Long eventId) {
        Veranstaltung event = eventId == null ? null : event(eventId);
        Kanu k = new Kanu(); applyKanu(k, input, event); return toKanu(kanuRepository.save(k));
    }
    public KanuInfo updateKanu(Long id, KanuInput input) {
        Kanu k = kanuRepository.findById(id).orElseThrow(() -> missing("Kanu nicht gefunden."));
        applyKanu(k, input, k.getVeranstaltung()); return toKanu(k);
    }
    public List<GewaesserInfo> gewaesser() { return gewaesserRepository.findAllByOrderByName().stream().map(g -> new GewaesserInfo(g.getId(), g.getName())).toList(); }
    public GewaesserInfo createGewaesser(GewaesserInput input) {
        String name = input.name().trim();
        if (gewaesserRepository.existsByNameIgnoreCase(name)) throw conflict("Dieses Gewässer ist bereits erfasst.");
        Gewaesser g = new Gewaesser(); g.setName(name); g = gewaesserRepository.save(g); return new GewaesserInfo(g.getId(), g.getName());
    }
    public List<FahrtInfo> fahrten(Long eventId) {
        event(eventId); return fahrtRepository.findByVeranstaltung_IdOrderByDatumAscBeginnZeitAsc(eventId).stream().map(this::toFahrt).toList();
    }
    public FahrtInfo createFahrt(Long eventId, FahrtInput input) { return saveFahrt(eventId, null, input); }
    public FahrtInfo updateFahrt(Long eventId, Long id, FahrtInput input) { return saveFahrt(eventId, id, input); }
    public BootInfo addBoot(Long eventId, Long fahrtId, BootInput input) {
        Fahrt f = ride(eventId, fahrtId);
        Kanu k = kanuRepository.findById(input.kanuId()).orElseThrow(() -> missing("Kanu nicht gefunden."));
        if (!k.isAktiv() || (k.getVeranstaltung() != null && !eventId.equals(k.getVeranstaltung().getId()))) throw conflict("Das Kanu ist in dieser Veranstaltung nicht verfügbar.");
        if (fahrtKanuRepository.findByFahrt_IdOrderById(fahrtId).stream().anyMatch(x -> x.getKanu().getId().equals(k.getId()))) throw conflict("Dieses Kanu ist für die Fahrt bereits eingeteilt.");
        FahrtKanu fk = new FahrtKanu(); fk.setFahrt(f); fk.setKanu(k); fk.setLizenzErforderlich(input.lizenzErforderlich());
        return toBoot(fahrtKanuRepository.save(fk));
    }
    public void removeBoot(Long eventId, Long fahrtId, Long fahrtKanuId) {
        ride(eventId, fahrtId);
        FahrtKanu fk = fahrtKanuRepository.findByIdAndFahrt_Id(fahrtKanuId, fahrtId).orElseThrow(() -> missing("Kanuzuordnung nicht gefunden."));
        if (!besatzungRepository.findByFahrtKanu_IdOrderById(fahrtKanuId).isEmpty()) throw conflict("Ein Kanu mit Besatzung kann nicht entfernt werden. Entferne zuerst die Besatzung.");
        fahrtKanuRepository.delete(fk);
    }
    public FahrtInfo addBesatzung(Long eventId, Long fahrtId, Long fahrtKanuId, BesatzungInput input) {
        Fahrt f = ride(eventId, fahrtId);
        FahrtKanu fk = fahrtKanuRepository.findByIdAndFahrt_Id(fahrtKanuId, fahrtId).orElseThrow(() -> missing("Kanuzuordnung nicht gefunden."));
        List<FahrtBesatzung> crew = besatzungRepository.findByFahrtKanu_IdOrderById(fahrtKanuId);
        if (crew.size() >= fk.getKanu().getPlatzanzahl()) throw conflict("Die Platzanzahl dieses Kanus ist erreicht.");
        Teilnehmer t = teilnehmerRepository.findByIdAndVeranstaltung_Id(input.teilnehmerId(), eventId).orElseThrow(() -> missing("Nur Veranstaltungsteilnehmer dürfen einer Besatzung zugeordnet werden."));
        if (besatzungRepository.findByFahrt_IdOrderById(fahrtId).stream().anyMatch(x -> x.getTeilnehmer().getId().equals(t.getId()))) throw conflict("Der Teilnehmer ist für diese Fahrt bereits einer Besatzung zugeordnet.");
        FahrtBesatzung b = new FahrtBesatzung(); b.setFahrt(f); b.setFahrtKanu(fk); b.setTeilnehmer(t); besatzungRepository.save(b);
        return toFahrt(f);
    }
    public FahrtInfo removeBesatzung(Long eventId, Long fahrtId, Long besatzungId) {
        Fahrt f = ride(eventId, fahrtId);
        FahrtBesatzung b = besatzungRepository.findById(besatzungId).orElseThrow(() -> missing("Besatzung nicht gefunden."));
        if (!b.getFahrt().getId().equals(fahrtId)) throw missing("Besatzung gehört nicht zu dieser Fahrt.");
        besatzungRepository.delete(b); return toFahrt(f);
    }
    public List<LizenzInfo> lizenzen(Long kanuId) { return lizenzRepository.findByKanu_IdOrderByGueltigVonDesc(kanuId).stream().map(this::toLizenz).toList(); }
    public LizenzInfo createLizenz(Long kanuId, LizenzInput input) {
        Kanu k = kanuRepository.findById(kanuId).orElseThrow(() -> missing("Kanu nicht gefunden."));
        if (input.gueltigBis().isBefore(input.gueltigVon())) throw conflict("Das Gültigkeitsende liegt vor dem Beginn.");
        KanuLizenz l = new KanuLizenz(); l.setKanu(k); l.setLizenznummer(input.lizenznummer().trim()); l.setGueltigVon(input.gueltigVon()); l.setGueltigBis(input.gueltigBis()); l.setKosten(input.kosten()); return toLizenz(lizenzRepository.save(l));
    }
    public List<KilometerInfo> kilometer(Long eventId, LocalDate von, LocalDate bis) {
        event(eventId);
        Map<String, Object[]> sums = new LinkedHashMap<>();
        for (Fahrt f : fahrtRepository.findByVeranstaltung_IdOrderByDatumAscBeginnZeitAsc(eventId)) {
            if ((von != null && f.getDatum().isBefore(von)) || (bis != null && f.getDatum().isAfter(bis))) continue;
            for (FahrtBesatzung b : besatzungRepository.findByFahrt_IdOrderById(f.getId())) {
                String key = b.getTeilnehmer().getPerson().getId() + ":" + f.getGewaesser().getId();
                Object[] v = sums.computeIfAbsent(key, __ -> new Object[]{b.getTeilnehmer().getPerson(), f.getGewaesser().getName(), java.math.BigDecimal.ZERO});
                v[2] = ((java.math.BigDecimal)v[2]).add(f.getKilometer());
            }
        }
        return sums.values().stream().map(v -> { Person p = (Person)v[0]; return new KilometerInfo(p.getId(), p.getVorname(), p.getName(), (String)v[1], (java.math.BigDecimal)v[2]); }).toList();
    }

    private FahrtInfo saveFahrt(Long eventId, Long id, FahrtInput input) {
        Veranstaltung event = event(eventId);
        if (input.datum().isBefore(event.getBeginnDatum()) || input.datum().isAfter(event.getEndeDatum())) throw conflict("Das Fahrtdatum muss innerhalb des Veranstaltungszeitraums liegen.");
        if (input.endeZeit() != null && input.beginnZeit() != null && input.endeZeit().isBefore(input.beginnZeit())) throw conflict("Das Ende liegt vor dem Beginn.");
        Fahrt f = id == null ? new Fahrt() : ride(eventId, id);
        f.setVeranstaltung(event); f.setDatum(input.datum()); f.setBeginnZeit(input.beginnZeit()); f.setEndeZeit(input.endeZeit());
        f.setGewaesser(gewaesserRepository.findById(input.gewaesserId()).orElseThrow(() -> missing("Gewässer nicht gefunden.")));
        f.setStartort(input.startort()); f.setZielort(input.zielort()); f.setKilometer(input.kilometer()); f.setStreckenangabe(input.streckenangabe()); f.setBemerkung(input.bemerkung());
        return toFahrt(fahrtRepository.save(f));
    }
    private Veranstaltung event(Long id) {
        Veranstaltung v = veranstaltungRepository.findById(id).orElseThrow(() -> missing("Veranstaltung nicht gefunden."));
        if (!v.isFahrtenAktiv()) throw conflict("Fahrtenverwaltung ist für diese Veranstaltung nicht aktiviert.");
        return v;
    }
    private Fahrt ride(Long eventId, Long fahrtId) { event(eventId); return fahrtRepository.findByIdAndVeranstaltung_Id(fahrtId, eventId).orElseThrow(() -> missing("Fahrt nicht gefunden.")); }
    private void applyKanu(Kanu k, KanuInput d, Veranstaltung event) {
        if (d.platzanzahl() < 1 || d.platzanzahl() > 4) throw conflict("Die Platzanzahl muss zwischen 1 und 4 liegen.");
        k.setKennzeichnung(d.kennzeichnung().trim()); k.setBezeichnung(d.bezeichnung().trim()); k.setTyp(d.typ()); k.setPlatzanzahl(d.platzanzahl()); k.setAktiv(d.aktiv()); k.setVeranstaltung(event);
        k.setEigentuemer(d.eigentuemerId() == null ? null : personRepository.findById(d.eigentuemerId()).orElseThrow(() -> missing("Eigentümer nicht gefunden.")));
    }
    private KanuInfo toKanu(Kanu k) { return new KanuInfo(k.getId(), k.getKennzeichnung(), k.getBezeichnung(), k.getTyp(), k.getPlatzanzahl(), k.isAktiv(), k.getVeranstaltung() != null, k.getVeranstaltung() == null ? null : k.getVeranstaltung().getId(), k.getEigentuemer() == null ? null : k.getEigentuemer().getId(), k.getEigentuemer() == null ? null : PersonName.givenFirst(k.getEigentuemer().getName(), k.getEigentuemer().getVorname())); }
    private FahrtInfo toFahrt(Fahrt f) { return new FahrtInfo(f.getId(), f.getVeranstaltung().getId(), f.getDatum(), f.getBeginnZeit(), f.getEndeZeit(), new GewaesserInfo(f.getGewaesser().getId(), f.getGewaesser().getName()), f.getStartort(), f.getZielort(), f.getKilometer(), f.getStreckenangabe(), f.getBemerkung(), fahrtKanuRepository.findByFahrt_IdOrderById(f.getId()).stream().map(this::toBoot).toList()); }
    private BootInfo toBoot(FahrtKanu fk) { var crew = besatzungRepository.findByFahrtKanu_IdOrderById(fk.getId()); boolean licensed = !fk.isLizenzErforderlich() || lizenzRepository.existsByKanu_IdAndGueltigVonLessThanEqualAndGueltigBisGreaterThanEqual(fk.getKanu().getId(), fk.getFahrt().getDatum(), fk.getFahrt().getDatum()); return new BootInfo(fk.getId(), toKanu(fk.getKanu()), fk.isLizenzErforderlich(), licensed, crew.stream().map(x -> new CrewInfo(x.getId(), x.getTeilnehmer().getId(), x.getTeilnehmer().getPerson().getId(), x.getTeilnehmer().getPerson().getVorname(), x.getTeilnehmer().getPerson().getName())).toList()); }
    private LizenzInfo toLizenz(KanuLizenz l) { return new LizenzInfo(l.getId(), l.getKanu().getId(), l.getLizenznummer(), l.getGueltigVon(), l.getGueltigBis(), l.getKosten()); }
    private ResponseStatusException missing(String m) { return new ResponseStatusException(HttpStatus.NOT_FOUND, m); }
    private ResponseStatusException conflict(String m) { return new ResponseStatusException(HttpStatus.CONFLICT, m); }
}
