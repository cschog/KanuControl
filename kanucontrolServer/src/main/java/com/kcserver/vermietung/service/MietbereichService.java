package com.kcserver.vermietung.service;

import com.kcserver.core.exception.ErrorMessages;
import com.kcserver.vermietung.dto.MietbereichDTO;
import com.kcserver.vermietung.dto.SortierungRequest;
import com.kcserver.vermietung.entity.Mietbereich;
import com.kcserver.vermietung.entity.Mietobjekt;
import com.kcserver.vermietung.entity.Zaehler;
import com.kcserver.vermietung.enumtype.Berechnungsart;
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

        Zaehler zaehler = findeZaehler(
                mietobjekt.getId(),
                dto.getZaehlerId()
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

        Zaehler zaehler = findeZaehler(
                mietobjektId,
                dto.getZaehlerId()
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

    private Zaehler findeZaehler(Long mietobjektId, Long zaehlerId) {

        if (zaehlerId == null) {
            return null;
        }

        Zaehler zaehler = zaehlerRepository.findById(zaehlerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        ErrorMessages.ZAEHLER_NOT_FOUND
                ));

        if (!zaehler.getMietobjekt().getId().equals(mietobjektId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.ZAEHLER_WRONG_MIETOBJEKT
            );
        }

        return zaehler;
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
        dto.setZaehlerId(
                bereich.getZaehler() != null
                        ? bereich.getZaehler().getId()
                        : null
        );

        return dto;
    }
}