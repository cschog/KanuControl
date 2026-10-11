package com.kcserver.vermietung.service;

import com.kcserver.core.exception.ErrorMessages;
import com.kcserver.vermietung.dto.ZaehlerDTO;
import com.kcserver.vermietung.entity.Mietobjekt;
import com.kcserver.vermietung.entity.Zaehler;
import com.kcserver.vermietung.repository.MietobjektRepository;
import com.kcserver.vermietung.repository.ZaehlerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class ZaehlerService {

    private final ZaehlerRepository zaehlerRepository;
    private final MietobjektRepository mietobjektRepository;

    public ZaehlerService(
            ZaehlerRepository zaehlerRepository,
            MietobjektRepository mietobjektRepository
    ) {
        this.zaehlerRepository = zaehlerRepository;
        this.mietobjektRepository = mietobjektRepository;
    }

    @Transactional(readOnly = true)
    public List<ZaehlerDTO> getAll(Long mietobjektId) {

        pruefeMietobjekt(mietobjektId);

        return zaehlerRepository
                .findByMietobjektIdOrderByIdAsc(mietobjektId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ZaehlerDTO getById(Long mietobjektId, Long id) {

        Zaehler zaehler = findeZaehler(mietobjektId, id);

        return toDTO(zaehler);
    }

    public ZaehlerDTO create(Long mietobjektId, ZaehlerDTO dto) {

        pruefeMietobjekt(mietobjektId);
        validiere(dto);

        Mietobjekt mietobjekt = mietobjektRepository
                .findById(mietobjektId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        ErrorMessages.MIETOBJEKT_NOT_FOUND
                ));

        Zaehler zaehler = new Zaehler();

        zaehler.setMietobjekt(mietobjekt);

        uebernehmeDaten(dto, zaehler);

        return toDTO(zaehlerRepository.save(zaehler));
    }

    public ZaehlerDTO update(
            Long mietobjektId,
            Long id,
            ZaehlerDTO dto
    ) {
        Zaehler zaehler = findeZaehler(mietobjektId, id);

        validiere(dto);

        uebernehmeDaten(dto, zaehler);

        return toDTO(zaehlerRepository.save(zaehler));
    }

    public void delete(Long mietobjektId, Long id) {

        Zaehler zaehler = findeZaehler(mietobjektId, id);

        zaehlerRepository.delete(zaehler);
    }

    private void pruefeMietobjekt(Long mietobjektId) {

        if (!mietobjektRepository.existsById(mietobjektId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    ErrorMessages.MIETOBJEKT_NOT_FOUND
            );
        }
    }

    private Zaehler findeZaehler(Long mietobjektId, Long id) {

        Zaehler zaehler = zaehlerRepository
                .findById(id)
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

    private void validiere(ZaehlerDTO dto) {

        if (dto.getBezeichnung() == null
                || dto.getBezeichnung().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.ZAEHLER_BEZEICHNUNG_REQUIRED
            );
        }

        if (dto.getVerbrauchsart() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.ZAEHLER_VERBRAUCHSART_REQUIRED
            );
        }

        if (dto.getMengeneinheit() == null
                || dto.getMengeneinheit().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.ZAEHLER_MENGENEINHEIT_REQUIRED
            );
        }

        if (dto.getBezeichnung().length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.ZAEHLER_BEZEICHNUNG_TOO_LONG
            );
        }

        if (dto.getZaehlernummer() != null
                && dto.getZaehlernummer().length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.ZAEHLER_NUMMER_TOO_LONG
            );
        }

        if (dto.getMengeneinheit().length() > 20) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.ZAEHLER_MENGENEINHEIT_TOO_LONG
            );
        }
    }

    private void uebernehmeDaten(
            ZaehlerDTO dto,
            Zaehler zaehler
    ) {
        zaehler.setBezeichnung(dto.getBezeichnung().trim());
        zaehler.setVerbrauchsart(dto.getVerbrauchsart());
        zaehler.setZaehlernummer(
                dto.getZaehlernummer() == null
                        || dto.getZaehlernummer().isBlank()
                        ? null
                        : dto.getZaehlernummer().trim()
        );
        zaehler.setMengeneinheit(dto.getMengeneinheit().trim());
        zaehler.setAktiv(dto.isAktiv());
    }

    private ZaehlerDTO toDTO(Zaehler zaehler) {

        ZaehlerDTO dto = new ZaehlerDTO();

        dto.setId(zaehler.getId());
        dto.setMietobjektId(zaehler.getMietobjekt().getId());
        dto.setBezeichnung(zaehler.getBezeichnung());
        dto.setVerbrauchsart(zaehler.getVerbrauchsart());
        dto.setZaehlernummer(zaehler.getZaehlernummer());
        dto.setMengeneinheit(zaehler.getMengeneinheit());
        dto.setAktiv(zaehler.isAktiv());

        return dto;
    }
}