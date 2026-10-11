package com.kcserver.vermietung.service;

import com.kcserver.core.exception.ErrorMessages;
import com.kcserver.vermietung.dto.MietobjektDTO;
import com.kcserver.vermietung.dto.SortierungRequest;
import com.kcserver.vermietung.entity.Mietobjekt;
import com.kcserver.vermietung.repository.MietobjektRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class MietobjektService {

    private final MietobjektRepository mietobjektRepository;

    public MietobjektService(MietobjektRepository mietobjektRepository) {
        this.mietobjektRepository = mietobjektRepository;
    }

    @Transactional(readOnly = true)
    public List<MietobjektDTO> getAll() {
        return mietobjektRepository.findAllByOrderBySortierungAscIdAsc()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public MietobjektDTO getById(Long id) {
        Mietobjekt objekt = mietobjektRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        ErrorMessages.MIETOBJEKT_NOT_FOUND
                ));

        return toDTO(objekt);
    }

    public MietobjektDTO create(MietobjektDTO dto) {

        Mietobjekt objekt = new Mietobjekt();

        objekt.setBezeichnung(dto.getBezeichnung());
        objekt.setBeschreibung(dto.getBeschreibung());
        objekt.setStrasse(dto.getStrasse());
        objekt.setPlz(dto.getPlz());
        objekt.setOrt(dto.getOrt());
        objekt.setCountryCode(dto.getCountryCode());
        objekt.setSortierung(dto.getSortierung());

        mietobjektRepository.unsetAktivesMietobjekt();
        mietobjektRepository.flush();

        objekt.setAktiv(true);
        objekt.setMietbar(dto.isMietbar());
        objekt.setDirektbuchungAktiv(dto.isDirektbuchungAktiv());
        objekt.setAirbnbAktiv(dto.isAirbnbAktiv());

        return toDTO(mietobjektRepository.save(objekt));
    }

    public MietobjektDTO update(Long id, MietobjektDTO dto) {

        Mietobjekt objekt = mietobjektRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        ErrorMessages.MIETOBJEKT_NOT_FOUND
                ));

        objekt.setBezeichnung(dto.getBezeichnung());
        objekt.setBeschreibung(dto.getBeschreibung());
        objekt.setStrasse(dto.getStrasse());
        objekt.setPlz(dto.getPlz());
        objekt.setOrt(dto.getOrt());
        objekt.setCountryCode(dto.getCountryCode());
        objekt.setSortierung(dto.getSortierung());
        objekt.setMietbar(dto.isMietbar());
        objekt.setDirektbuchungAktiv(dto.isDirektbuchungAktiv());
        objekt.setAirbnbAktiv(dto.isAirbnbAktiv());

        if (dto.isAktiv() && !objekt.isAktiv()) {
            mietobjektRepository.unsetAktivesMietobjekt();
            mietobjektRepository.flush();
        }

        objekt.setAktiv(dto.isAktiv());

        return toDTO(mietobjektRepository.save(objekt));
    }

    public void delete(Long id) {

        Mietobjekt objekt = mietobjektRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        ErrorMessages.MIETOBJEKT_NOT_FOUND
                ));

        boolean warAktiv = objekt.isAktiv();

        mietobjektRepository.delete(objekt);
        mietobjektRepository.flush();

        if (warAktiv) {
            mietobjektRepository.findFirstByOrderByIdAsc()
                    .ifPresent(neuAktiv -> {
                        neuAktiv.setAktiv(true);
                        mietobjektRepository.save(neuAktiv);
                    });
        }
    }

    private MietobjektDTO toDTO(Mietobjekt objekt) {

        MietobjektDTO dto = new MietobjektDTO();

        dto.setId(objekt.getId());
        dto.setBezeichnung(objekt.getBezeichnung());
        dto.setBeschreibung(objekt.getBeschreibung());
        dto.setStrasse(objekt.getStrasse());
        dto.setPlz(objekt.getPlz());
        dto.setOrt(objekt.getOrt());
        dto.setCountryCode(objekt.getCountryCode());
        dto.setSortierung(objekt.getSortierung());
        dto.setAktiv(objekt.isAktiv());
        dto.setMietbar(objekt.isMietbar());
        dto.setDirektbuchungAktiv(objekt.isDirektbuchungAktiv());
        dto.setAirbnbAktiv(objekt.isAirbnbAktiv());

        return dto;
    }

    public MietobjektDTO setActive(Long mietobjektId) {

        Mietobjekt neu = mietobjektRepository.findById(mietobjektId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        ErrorMessages.MIETOBJEKT_NOT_FOUND
                ));

        mietobjektRepository.unsetAktivesMietobjekt();
        mietobjektRepository.flush();

        neu.setAktiv(true);
        mietobjektRepository.save(neu);

        return toDTO(neu);
    }

    @Transactional(readOnly = true)
    public MietobjektDTO getActive() {
        return mietobjektRepository.findByAktivTrue()
                .map(this::toDTO)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        ErrorMessages.MIETOBJEKT_NO_ACTIVE
                ));
    }

    public void sortieren(SortierungRequest request) {

        List<Long> ids = request.ids();

        // Doppelte IDs verhindern
        if (new HashSet<>(ids).size() != ids.size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.MIETOBJEKT_SORTIERUNG_DUPLICATE_IDS
            );
        }

        // Alle vorhandenen Mietobjekte laden
        List<Mietobjekt> alleObjekte = mietobjektRepository.findAll();

        Set<Long> alleIds = alleObjekte.stream()
                .map(Mietobjekt::getId)
                .collect(Collectors.toSet());

        // Prüfen, ob alle Mietobjekte genau einmal enthalten sind
        if (!alleIds.equals(new HashSet<>(ids))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.MIETOBJEKT_SORTIERUNG_INCOMPLETE
            );
        }

        // Gewünschte Reihenfolge übernehmen
        Map<Long, Mietobjekt> objekteNachId = alleObjekte.stream()
                .collect(Collectors.toMap(
                        Mietobjekt::getId,
                        objekt -> objekt
                ));

        for (int i = 0; i < ids.size(); i++) {
            objekteNachId.get(ids.get(i)).setSortierung(i + 1);
        }

        mietobjektRepository.saveAll(alleObjekte);
    }
}