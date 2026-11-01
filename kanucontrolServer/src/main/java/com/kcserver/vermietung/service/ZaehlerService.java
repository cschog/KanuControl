package com.kcserver.vermietung.service;

import com.kcserver.core.exception.ErrorMessages;
import com.kcserver.vermietung.dto.ZaehlerDTO;
import com.kcserver.vermietung.entity.Zaehler;
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

    public ZaehlerService(
            ZaehlerRepository zaehlerRepository
    ) {
        this.zaehlerRepository = zaehlerRepository;
    }

    @Transactional(readOnly = true)
    public List<ZaehlerDTO> getAll() {

        return zaehlerRepository
                .findAllByOrderByIdAsc()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ZaehlerDTO getById(Long id) {

        Zaehler zaehler = findeZaehler(id);

        return toDTO(zaehler);
    }

    public ZaehlerDTO create(ZaehlerDTO dto) {

        validiere(dto);

        Zaehler zaehler = new Zaehler();

        uebernehmeDaten(dto, zaehler);

        return toDTO(zaehlerRepository.save(zaehler));
    }

    public ZaehlerDTO update(
            Long id,
            ZaehlerDTO dto
    ) {
        Zaehler zaehler = findeZaehler(id);

        validiere(dto);
        uebernehmeDaten(dto, zaehler);

        return toDTO(zaehlerRepository.save(zaehler));
    }

    public void delete(Long id) {

        Zaehler zaehler = findeZaehler(id);

        zaehlerRepository.delete(zaehler);
    }

    private Zaehler findeZaehler(Long id) {

        return zaehlerRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        ErrorMessages.ZAEHLER_NOT_FOUND
                ));
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
        dto.setBezeichnung(zaehler.getBezeichnung());
        dto.setVerbrauchsart(zaehler.getVerbrauchsart());
        dto.setZaehlernummer(zaehler.getZaehlernummer());
        dto.setMengeneinheit(zaehler.getMengeneinheit());
        dto.setAktiv(zaehler.isAktiv());

        return dto;
    }
}