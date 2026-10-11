package com.kcserver.vermietung.service;

import com.kcserver.vermietung.dto.VerbrauchskostenDTO;
import com.kcserver.vermietung.entity.Mietobjekt;
import com.kcserver.vermietung.entity.Verbrauchskosten;
import com.kcserver.vermietung.enumtype.Verbrauchsabrechnungsart;
import com.kcserver.vermietung.repository.MietobjektRepository;
import com.kcserver.vermietung.repository.VerbrauchskostenRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.kcserver.core.exception.ErrorMessages;

import java.util.List;

@Service
@Transactional
public class VerbrauchskostenService {

    private final VerbrauchskostenRepository verbrauchskostenRepository;
    private final MietobjektRepository mietobjektRepository;

    public VerbrauchskostenService(
            VerbrauchskostenRepository verbrauchskostenRepository,
            MietobjektRepository mietobjektRepository
    ) {
        this.verbrauchskostenRepository = verbrauchskostenRepository;
        this.mietobjektRepository = mietobjektRepository;
    }

    @Transactional(readOnly = true)
    public List<VerbrauchskostenDTO> getAll(Long mietobjektId) {

        pruefeMietobjekt(mietobjektId);

        return verbrauchskostenRepository
                .findByMietobjektIdOrderByIdAsc(mietobjektId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public VerbrauchskostenDTO getById(Long mietobjektId, Long id) {

        Verbrauchskosten verbrauchskosten = findeVerbrauchskosten(mietobjektId, id);

        return toDTO(verbrauchskosten);
    }

    public VerbrauchskostenDTO create(VerbrauchskostenDTO dto) {

        validiere(dto);

        Mietobjekt mietobjekt = mietobjektRepository
                .findById(dto.getMietobjektId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        ErrorMessages.MIETOBJEKT_NOT_FOUND
                ));

        Verbrauchskosten verbrauchskosten = new Verbrauchskosten();

        verbrauchskosten.setMietobjekt(mietobjekt);

        uebernehmeDaten(dto, verbrauchskosten);

        return toDTO(verbrauchskostenRepository.save(verbrauchskosten));
    }

    public VerbrauchskostenDTO update(
            Long mietobjektId,
            Long id,
            VerbrauchskostenDTO dto
    ) {
        Verbrauchskosten verbrauchskosten = findeVerbrauchskosten(mietobjektId, id);

        validiere(dto);

        verbrauchskosten.setBezeichnung(dto.getBezeichnung().trim());
        verbrauchskosten.setVerbrauchsart(dto.getVerbrauchsart());
        verbrauchskosten.setAbrechnungsart(dto.getAbrechnungsart());
        verbrauchskosten.setMengeneinheit(dto.getMengeneinheit());
        verbrauchskosten.setPreis(dto.getPreis());
        verbrauchskosten.setAktiv(dto.isAktiv());

        return toDTO(verbrauchskostenRepository.save(verbrauchskosten));
    }

    public void delete(Long mietobjektId, Long id) {

        Verbrauchskosten verbrauchskosten = findeVerbrauchskosten(mietobjektId, id);

        verbrauchskostenRepository.delete(verbrauchskosten);
    }

    private void pruefeMietobjekt(Long mietobjektId) {

        if (!mietobjektRepository.existsById(mietobjektId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    ErrorMessages.MIETOBJEKT_NOT_FOUND
            );
        }
    }

    private Verbrauchskosten findeVerbrauchskosten(
            Long mietobjektId,
            Long id
    ) {
        Verbrauchskosten verbrauchskosten = verbrauchskostenRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        ErrorMessages.VERBRAUCHSKOSTEN_NOT_FOUND
                ));

        if (!verbrauchskosten.getMietobjekt().getId().equals(mietobjektId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.VERBRAUCHSKOSTEN_WRONG_MIETOBJEKT
            );
        }

        return verbrauchskosten;
    }

    private void validiere(VerbrauchskostenDTO dto) {

        if (dto.getVerbrauchsart() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.VERBRAUCHSKOSTEN_VERBRAUCHSART_REQUIRED
            );
        }

        if (dto.getBezeichnung() == null || dto.getBezeichnung().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.VERBRAUCHSKOSTEN_BEZEICHNUNG_REQUIRED
            );
        }

        if (dto.getAbrechnungsart() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.VERBRAUCHSKOSTEN_ABRECHNUNGSART_REQUIRED
            );
        }

        if (dto.getPreis() == null || dto.getPreis().signum() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.VERBRAUCHSKOSTEN_PREIS_INVALID
            );
        }

        if (dto.getAbrechnungsart() == Verbrauchsabrechnungsart.ZAEHLERSTAND) {

            if (dto.getMengeneinheit() == null
                    || dto.getMengeneinheit().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        ErrorMessages.VERBRAUCHSKOSTEN_MENGENEINHEIT_REQUIRED
                );
            }

        } else if (dto.getMengeneinheit() != null
                && !dto.getMengeneinheit().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.VERBRAUCHSKOSTEN_MENGENEINHEIT_NOT_ALLOWED
            );
        }
    }

    private void uebernehmeDaten(
            VerbrauchskostenDTO dto,
            Verbrauchskosten verbrauchskosten
    ) {
        verbrauchskosten.setBezeichnung(dto.getBezeichnung().trim());
        verbrauchskosten.setVerbrauchsart(dto.getVerbrauchsart());
        verbrauchskosten.setAbrechnungsart(dto.getAbrechnungsart());
        verbrauchskosten.setMengeneinheit(
                dto.getAbrechnungsart() == Verbrauchsabrechnungsart.ZAEHLERSTAND
                        ? dto.getMengeneinheit().trim()
                        : null
        );
        verbrauchskosten.setPreis(dto.getPreis());
        verbrauchskosten.setAktiv(dto.isAktiv());
    }

    private VerbrauchskostenDTO toDTO(Verbrauchskosten verbrauchskosten) {

        VerbrauchskostenDTO dto = new VerbrauchskostenDTO();

        dto.setId(verbrauchskosten.getId());
        dto.setMietobjektId(verbrauchskosten.getMietobjekt().getId());
        dto.setBezeichnung(verbrauchskosten.getBezeichnung());
        dto.setVerbrauchsart(verbrauchskosten.getVerbrauchsart());
        dto.setAbrechnungsart(verbrauchskosten.getAbrechnungsart());
        dto.setMengeneinheit(verbrauchskosten.getMengeneinheit());
        dto.setPreis(verbrauchskosten.getPreis());
        dto.setAktiv(verbrauchskosten.isAktiv());

        return dto;
    }
}