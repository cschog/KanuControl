package com.kcserver.vermietung.service;

import com.kcserver.core.exception.ErrorMessages;
import com.kcserver.vermietung.dto.MietbereichDTO;
import com.kcserver.vermietung.dto.SortierungRequest;
import com.kcserver.vermietung.entity.Mietbereich;
import com.kcserver.vermietung.entity.Mietobjekt;
import com.kcserver.vermietung.entity.Zaehler;
import com.kcserver.vermietung.enumtype.Berechnungsart;
import com.kcserver.vermietung.enumtype.Buchungsquelle;
import com.kcserver.vermietung.dto.MietbereichVoraussetzungenDTO;
import com.kcserver.vermietung.repository.MietbereichRepository;
import com.kcserver.vermietung.repository.MietobjektRepository;
import com.kcserver.vermietung.repository.ZaehlerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class MietbereichService {

    private final MietbereichRepository mietbereichRepository;
    private final MietobjektRepository mietobjektRepository;
    private final ZaehlerRepository zaehlerRepository;

    public MietbereichService(
            MietbereichRepository mietbereichRepository,
            MietobjektRepository mietobjektRepository,
            ZaehlerRepository zaehlerRepository
    ) {
        this.mietbereichRepository = mietbereichRepository;
        this.mietobjektRepository = mietobjektRepository;
        this.zaehlerRepository = zaehlerRepository;
    }

    @Transactional(readOnly = true)
    public List<MietbereichDTO> getAll(Long mietobjektId) {

        if (!mietobjektRepository.existsById(mietobjektId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    ErrorMessages.MIETOBJEKT_NOT_FOUND
            );
        }

        return mietbereichRepository
                .findByMietobjektIdOrderBySortierungAscIdAsc(mietobjektId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public MietbereichDTO getById(Long mietobjektId, Long id) {

        Mietbereich bereich = mietbereichRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        ErrorMessages.MIETBEREICH_NOT_FOUND
                ));

        if (!bereich.getMietobjekt().getId().equals(mietobjektId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.MIETBEREICH_WRONG_MIETOBJEKT
            );
        }

        return toDTO(bereich);
    }

    public MietbereichDTO create(MietbereichDTO dto) {

        validateAirbnbInserat(dto);

        Mietobjekt mietobjekt = mietobjektRepository
                .findById(dto.getMietobjektId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        ErrorMessages.MIETOBJEKT_NOT_FOUND
                ));

        Set<Zaehler> zaehler = findeZaehler(
                mietobjekt.getId(),
                dto.getZaehlerIds()
        );

        Mietbereich bereich = new Mietbereich();

        bereich.setMietobjekt(mietobjekt);
        bereich.setBezeichnung(dto.getBezeichnung());
        bereich.setBeschreibung(dto.getBeschreibung());
        bereich.setMietbar(dto.isMietbar());
        bereich.setDirektbuchungAktiv(dto.isDirektbuchungAktiv());
        bereich.setAirbnbAktiv(dto.isAirbnbAktiv());
        bereich.setAirbnbInserat(dto.isAirbnbInserat());
        bereich.setZaehler(zaehler);
        bereich.setBerechnungsart(
                dto.getBerechnungsart() != null
                        ? dto.getBerechnungsart()
                        : Berechnungsart.TAG
        );
        bereich.setBestand(dto.getBestand());
        bereich.setMengeneinheit(dto.getMengeneinheit());
        bereich.setSortierung(dto.getSortierung());

        return toDTO(mietbereichRepository.save(bereich));
    }

    public MietbereichDTO update(
            Long mietobjektId,
            Long id,
            MietbereichDTO dto
    ) {
        Mietbereich bereich = mietbereichRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        ErrorMessages.MIETBEREICH_NOT_FOUND
                ));

        if (!bereich.getMietobjekt().getId().equals(mietobjektId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.MIETBEREICH_WRONG_MIETOBJEKT
            );
        }

        validateAirbnbInserat(dto);

        Set<Zaehler> zaehler = findeZaehler(
                mietobjektId,
                dto.getZaehlerIds()
        );

        bereich.setZaehler(zaehler);
        bereich.setBezeichnung(dto.getBezeichnung());
        bereich.setBeschreibung(dto.getBeschreibung());
        bereich.setMietbar(dto.isMietbar());
        bereich.setDirektbuchungAktiv(dto.isDirektbuchungAktiv());
        bereich.setAirbnbAktiv(dto.isAirbnbAktiv());
        bereich.setAirbnbInserat(dto.isAirbnbInserat());

        if (dto.getBerechnungsart() != null) {
            bereich.setBerechnungsart(dto.getBerechnungsart());
        }

        bereich.setBestand(dto.getBestand());
        bereich.setMengeneinheit(dto.getMengeneinheit());
        bereich.setSortierung(dto.getSortierung());

        return toDTO(mietbereichRepository.save(bereich));
    }

    public MietbereichDTO setVoraussetzungen(
            Long mietobjektId,
            Long mietbereichId,
            MietbereichVoraussetzungenDTO dto
    ) {
        Mietbereich bereich = mietbereichRepository.findById(mietbereichId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        ErrorMessages.MIETBEREICH_NOT_FOUND
                ));

        if (!bereich.getMietobjekt().getId().equals(mietobjektId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.MIETBEREICH_WRONG_MIETOBJEKT
            );
        }

        if (!bereich.isMietbar()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Für einen nicht mietbaren Mietbereich können keine Voraussetzungen festgelegt werden."
            );
        }
        Set<Long> direktIds = dto.getErforderlicheMietbereichIdsDirekt() != null
                ? dto.getErforderlicheMietbereichIdsDirekt()
                : dto.getErforderlicheMietbereichIds();
        Set<Mietbereich> direkt = validiereRegelBereiche(
                mietobjektId, mietbereichId, direktIds, "Voraussetzungen"
        );
        Set<Mietbereich> airbnb = validiereRegelBereiche(
                mietobjektId,
                mietbereichId,
                dto.getErforderlicheMietbereichIdsAirbnb() == null
                        ? bereich.getVoraussetzungenAirbnb().stream().map(Mietbereich::getId).collect(Collectors.toSet())
                        : dto.getErforderlicheMietbereichIdsAirbnb(),
                "Voraussetzungen"
        );
        Set<Mietbereich> belegt = validiereRegelBereiche(
                mietobjektId,
                mietbereichId,
                dto.getBelegteMietbereichIds() == null
                        ? bereich.getBelegteMietbereiche().stream().map(Mietbereich::getId).collect(Collectors.toSet())
                        : dto.getBelegteMietbereichIds(),
                "Belegte Mietbereiche"
        );

        if (direkt.stream().anyMatch(item -> !item.isDirektbuchungAktiv())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Direkt-Voraussetzungen müssen für Direktbuchungen aktiviert sein."
            );
        }
        if (airbnb.stream().anyMatch(item -> !item.isAirbnbAktiv())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Airbnb-Voraussetzungen müssen für Airbnb-Buchungen aktiviert sein."
            );
        }

        validiereVoraussetzungsKreis(bereich, direkt, Buchungsquelle.DIREKT);
        validiereVoraussetzungsKreis(bereich, airbnb, Buchungsquelle.AIRBNB);
        for (Mietbereich komponent : belegt) {
            if (erreichtBelegung(komponent, mietbereichId, new HashSet<>())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Die Belegungszusammensetzung würde einen Kreis erzeugen."
                );
            }
        }

        bereich.setVoraussetzungenDirekt(direkt);
        bereich.setVoraussetzungenAirbnb(airbnb);
        bereich.setBelegteMietbereiche(belegt);
        return toDTO(mietbereichRepository.save(bereich));
    }

    private Set<Mietbereich> validiereRegelBereiche(
            Long mietobjektId,
            Long mietbereichId,
            Set<Long> angeforderteIds,
            String feldname
    ) {
        Set<Long> ids = angeforderteIds == null ? Set.of() : angeforderteIds;
        if (ids.contains(mietbereichId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ein Mietbereich kann nicht sich selbst voraussetzen oder belegen."
            );
        }
        List<Mietbereich> bereiche = mietbereichRepository.findAllById(ids);
        if (bereiche.size() != ids.size()
                || bereiche.stream().anyMatch(item ->
                !item.getMietobjekt().getId().equals(mietobjektId) || !item.isMietbar())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    feldname + " müssen mietbare Bereiche desselben Mietobjekts sein."
            );
        }
        return new HashSet<>(bereiche);
    }

    private void validiereVoraussetzungsKreis(
            Mietbereich bereich,
            Set<Mietbereich> neueVoraussetzungen,
            Buchungsquelle quelle
    ) {
        for (Mietbereich voraussetzung : neueVoraussetzungen) {
            if (erreichtMietbereich(voraussetzung, bereich.getId(), new HashSet<>(), quelle)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Die Abhängigkeit würde einen Kreis erzeugen."
                );
            }
        }
    }

    private boolean erreichtMietbereich(
            Mietbereich start,
            Long gesucht,
            Set<Long> besucht,
            Buchungsquelle quelle
    ) {
        if (start.getId().equals(gesucht)) {
            return true;
        }
        if (!besucht.add(start.getId())) {
            return false;
        }
        Set<Mietbereich> voraussetzungen = quelle == Buchungsquelle.DIREKT
                ? start.getVoraussetzungenDirekt()
                : start.getVoraussetzungenAirbnb();
        return voraussetzungen.stream()
                .anyMatch(voraussetzung -> erreichtMietbereich(
                        voraussetzung,
                        gesucht,
                        besucht,
                        quelle
                ));
    }

    private boolean erreichtBelegung(Mietbereich start, Long gesucht, Set<Long> besucht) {
        if (start.getId().equals(gesucht)) return true;
        if (!besucht.add(start.getId())) return false;
        return start.getBelegteMietbereiche().stream()
                .anyMatch(kind -> erreichtBelegung(kind, gesucht, besucht));
    }

    public void delete(Long mietobjektId, Long id) {

        Mietbereich bereich = mietbereichRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        ErrorMessages.MIETBEREICH_NOT_FOUND
                ));

        if (!bereich.getMietobjekt().getId().equals(mietobjektId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.MIETBEREICH_WRONG_MIETOBJEKT
            );
        }

        mietbereichRepository.delete(bereich);
    }

    public void sortieren(Long mietobjektId, SortierungRequest request) {

        if (!mietobjektRepository.existsById(mietobjektId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    ErrorMessages.MIETOBJEKT_NOT_FOUND
            );
        }

        List<Long> ids = request.ids();

        if (new HashSet<>(ids).size() != ids.size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.MIETBEREICH_SORTIERUNG_DUPLICATE_IDS
            );
        }

        List<Mietbereich> alleBereiche =
                mietbereichRepository.findByMietobjektIdOrderBySortierungAscIdAsc(
                        mietobjektId
                );

        Set<Long> alleIds = alleBereiche.stream()
                .map(Mietbereich::getId)
                .collect(Collectors.toSet());

        if (!alleIds.equals(new HashSet<>(ids))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.MIETBEREICH_SORTIERUNG_INCOMPLETE
            );
        }

        Map<Long, Mietbereich> bereicheNachId = alleBereiche.stream()
                .collect(Collectors.toMap(
                        Mietbereich::getId,
                        bereich -> bereich
                ));

        for (int i = 0; i < ids.size(); i++) {
            bereicheNachId.get(ids.get(i)).setSortierung(i + 1);
        }

        mietbereichRepository.saveAll(alleBereiche);
    }

    private Set<Zaehler> findeZaehler(
            Long mietobjektId,
            Set<Long> zaehlerIds
    ) {
        Set<Zaehler> ergebnis = new HashSet<>();

        if (zaehlerIds == null || zaehlerIds.isEmpty()) {
            return ergebnis;
        }

        for (Long zaehlerId : zaehlerIds) {
            if (zaehlerId == null) {
                continue;
            }

            Zaehler zaehler = zaehlerRepository.findById(zaehlerId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            ErrorMessages.ZAEHLER_NOT_FOUND
                    ));

            ergebnis.add(zaehler);
        }

        return ergebnis;
    }

    private void validateAirbnbInserat(MietbereichDTO dto) {

        if (dto.isAirbnbInserat() && !dto.isAirbnbAktiv()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ein Airbnb-Inserat setzt voraus, dass Airbnb aktiv ist."
            );
        }
    }

    private MietbereichDTO toDTO(Mietbereich bereich) {

        MietbereichDTO dto = new MietbereichDTO();

        dto.setId(bereich.getId());
        dto.setMietobjektId(bereich.getMietobjekt().getId());
        dto.setBezeichnung(bereich.getBezeichnung());
        dto.setBeschreibung(bereich.getBeschreibung());
        dto.setMietbar(bereich.isMietbar());
        dto.setDirektbuchungAktiv(bereich.isDirektbuchungAktiv());
        dto.setAirbnbAktiv(bereich.isAirbnbAktiv());
        dto.setAirbnbInserat(bereich.isAirbnbInserat());
        dto.setBestand(bereich.getBestand());
        dto.setMengeneinheit(bereich.getMengeneinheit());
        dto.setSortierung(bereich.getSortierung());
        dto.setBerechnungsart(bereich.getBerechnungsart());
        dto.setErforderlicheMietbereichIdsDirekt(
                bereich.getVoraussetzungenDirekt() == null
                        ? new HashSet<>()
                        : bereich.getVoraussetzungenDirekt().stream()
                        .map(Mietbereich::getId)
                        .collect(Collectors.toSet())
        );
        dto.setErforderlicheMietbereichIds(
                dto.getErforderlicheMietbereichIdsDirekt()
        );
        dto.setErforderlicheMietbereichIdsAirbnb(
                bereich.getVoraussetzungenAirbnb() == null
                        ? new HashSet<>()
                        : bereich.getVoraussetzungenAirbnb().stream()
                        .map(Mietbereich::getId)
                        .collect(Collectors.toSet())
        );
        dto.setBelegteMietbereichIds(
                bereich.getBelegteMietbereiche() == null
                        ? new HashSet<>()
                        : bereich.getBelegteMietbereiche().stream()
                        .map(Mietbereich::getId)
                        .collect(Collectors.toSet())
        );
        dto.setZaehlerIds(
                bereich.getZaehler() == null
                        ? new HashSet<>()
                        : bereich.getZaehler().stream()
                        .map(Zaehler::getId)
                        .collect(Collectors.toSet())
        );

        return dto;
    }
}
