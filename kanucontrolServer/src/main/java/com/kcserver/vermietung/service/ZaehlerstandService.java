package com.kcserver.vermietung.service;

import com.kcserver.core.exception.ErrorMessages;
import com.kcserver.vermietung.dto.ZaehlerstandDTO;
import com.kcserver.vermietung.entity.Zaehler;
import com.kcserver.vermietung.entity.Zaehlerstand;
import com.kcserver.vermietung.repository.ZaehlerRepository;
import com.kcserver.vermietung.repository.ZaehlerstandRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class ZaehlerstandService {

    private final ZaehlerstandRepository zaehlerstandRepository;
    private final ZaehlerRepository zaehlerRepository;

    public ZaehlerstandService(
            ZaehlerstandRepository zaehlerstandRepository,
            ZaehlerRepository zaehlerRepository
    ) {
        this.zaehlerstandRepository = zaehlerstandRepository;
        this.zaehlerRepository = zaehlerRepository;
    }

    @Transactional(readOnly = true)
    public List<ZaehlerstandDTO> getAll(Long zaehlerId) {

        pruefeZaehler(zaehlerId);

        return zaehlerstandRepository
                .findByZaehlerIdOrderByAblesedatumDesc(zaehlerId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ZaehlerstandDTO getById(Long zaehlerId, Long id) {

        Zaehlerstand zaehlerstand = findeZaehlerstand(zaehlerId, id);

        return toDTO(zaehlerstand);
    }

    public ZaehlerstandDTO create(
            Long zaehlerId,
            ZaehlerstandDTO dto
    ) {
        Zaehler zaehler = findeZaehler(zaehlerId);

        validiere(dto);

        Zaehlerstand zaehlerstand = new Zaehlerstand();
        zaehlerstand.setZaehler(zaehler);

        uebernehmeDaten(dto, zaehlerstand);

        return toDTO(zaehlerstandRepository.save(zaehlerstand));
    }

    public ZaehlerstandDTO update(
            Long zaehlerId,
            Long id,
            ZaehlerstandDTO dto
    ) {
        Zaehlerstand zaehlerstand = findeZaehlerstand(zaehlerId, id);

        validiere(dto);
        uebernehmeDaten(dto, zaehlerstand);

        return toDTO(zaehlerstandRepository.save(zaehlerstand));
    }

    public void delete(Long zaehlerId, Long id) {

        Zaehlerstand zaehlerstand = findeZaehlerstand(zaehlerId, id);

        zaehlerstandRepository.delete(zaehlerstand);
    }

    private void pruefeZaehler(Long zaehlerId) {

        if (!zaehlerRepository.existsById(zaehlerId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    ErrorMessages.ZAEHLER_NOT_FOUND
            );
        }
    }

    private Zaehler findeZaehler(Long zaehlerId) {

        return zaehlerRepository
                .findById(zaehlerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        ErrorMessages.ZAEHLER_NOT_FOUND
                ));
    }

    private Zaehlerstand findeZaehlerstand(
            Long zaehlerId,
            Long id
    ) {
        Zaehlerstand zaehlerstand = zaehlerstandRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        ErrorMessages.ZAEHLERSTAND_NOT_FOUND
                ));

        if (!zaehlerstand.getZaehler().getId().equals(zaehlerId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.ZAEHLERSTAND_WRONG_ZAEHLER
            );
        }

        return zaehlerstand;
    }

    private void validiere(ZaehlerstandDTO dto) {

        if (dto.getAblesedatum() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.ZAEHLERSTAND_DATUM_REQUIRED
            );
        }

        if (dto.getZaehlerstand() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.ZAEHLERSTAND_WERT_REQUIRED
            );
        }

        if (dto.getZaehlerstand().signum() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.ZAEHLERSTAND_WERT_INVALID
            );
        }

        if (dto.getBemerkung() != null
                && dto.getBemerkung().length() > 1000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.INVALID_REQUEST
            );
        }
    }

    private void uebernehmeDaten(
            ZaehlerstandDTO dto,
            Zaehlerstand zaehlerstand
    ) {
        zaehlerstand.setAblesedatum(dto.getAblesedatum());
        zaehlerstand.setZaehlerstand(dto.getZaehlerstand());
        zaehlerstand.setBemerkung(
                dto.getBemerkung() == null || dto.getBemerkung().isBlank()
                        ? null
                        : dto.getBemerkung().trim()
        );
    }

    private ZaehlerstandDTO toDTO(Zaehlerstand zaehlerstand) {

        ZaehlerstandDTO dto = new ZaehlerstandDTO();

        dto.setId(zaehlerstand.getId());
        dto.setZaehlerId(zaehlerstand.getZaehler().getId());
        dto.setAblesedatum(zaehlerstand.getAblesedatum());
        dto.setZaehlerstand(zaehlerstand.getZaehlerstand());
        dto.setBemerkung(zaehlerstand.getBemerkung());

        return dto;
    }
}