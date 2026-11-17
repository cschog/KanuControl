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
        verbrauchskostenRepository.findForUpdate(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, ErrorMessages.VERBRAUCHSKOSTEN_NOT_FOUND));
        Verbrauchskosten verbrauchskosten = findeVerbrauchskosten(mietobjektId, id);

        validiere(dto);

        uebernehmePreise(dto, verbrauchskosten);
        verbrauchskosten.setBezeichnung(dto.getBezeichnung().trim());
        verbrauchskosten.setVerbrauchsart(dto.getVerbrauchsart());
        verbrauchskosten.setAbrechnungsart(dto.getAbrechnungsart());
        verbrauchskosten.setMengeneinheit(dto.getMengeneinheit());
        verbrauchskosten.setPreis(verbrauchskosten.getPreise().get(verbrauchskosten.getPreise().size() - 1).getPreis());
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

        if (dto.getPreise() == null && (dto.getPreis() == null || dto.getPreis().signum() < 0)) {
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
        uebernehmePreise(dto, verbrauchskosten);
        verbrauchskosten.setBezeichnung(dto.getBezeichnung().trim());
        verbrauchskosten.setVerbrauchsart(dto.getVerbrauchsart());
        verbrauchskosten.setAbrechnungsart(dto.getAbrechnungsart());
        verbrauchskosten.setMengeneinheit(
                dto.getAbrechnungsart() == Verbrauchsabrechnungsart.ZAEHLERSTAND
                        ? dto.getMengeneinheit().trim()
                        : null
        );
        verbrauchskosten.setPreis(verbrauchskosten.getPreise().get(verbrauchskosten.getPreise().size() - 1).getPreis());
        verbrauchskosten.setAktiv(dto.isAktiv());
    }

    private void uebernehmePreise(VerbrauchskostenDTO dto, Verbrauchskosten entity) {
        // Ältere Clients dürfen beim Bearbeiten keine vorhandene Historie überschreiben.
        if (dto.getPreise() == null && !entity.getPreise().isEmpty()) return;
        var preise = dto.getPreise() == null
                ? new java.util.ArrayList<>(List.of(new com.kcserver.vermietung.entity.Verbrauchspreis(
                    java.time.LocalDate.of(1, 1, 1), null, dto.getPreis())))
                : new java.util.ArrayList<>(dto.getPreise());
        if (preise.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mindestens einen Preis erfassen.");
        for (var p : preise) {
            if (p == null || p.getGueltigVon() == null || p.getPreis() == null || p.getPreis().signum() < 0
                    || p.getPreis().scale() > 2 || p.getPreis().precision() - p.getPreis().scale() > 8
                    || (p.getGueltigBis() != null && p.getGueltigBis().isBefore(p.getGueltigVon())))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ungültiger Preis oder Gültigkeitszeitraum.");
        }
        preise.sort(java.util.Comparator.comparing(com.kcserver.vermietung.entity.Verbrauchspreis::getGueltigVon));
        for (int i = 1; i < preise.size(); i++) {
            var vorher = preise.get(i - 1); var aktuell = preise.get(i);
            if (vorher.getGueltigVon().equals(aktuell.getGueltigVon()))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mehrere Preise mit demselben Gültigkeitsbeginn sind nicht erlaubt.");
            // Ein neuer Beginn begrenzt den vorhergehenden Zeitraum automatisch.
            if (vorher.getGueltigBis() == null || !vorher.getGueltigBis().isBefore(aktuell.getGueltigVon()))
            {
                boolean unveraendert = entity.getPreise().stream().anyMatch(alt ->
                        alt.getGueltigVon().equals(vorher.getGueltigVon())
                        && java.util.Objects.equals(alt.getGueltigBis(), vorher.getGueltigBis()));
                if (vorher.getGueltigBis() != null && !unveraendert)
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Die Preiszeiträume überschneiden sich. Bitte das Enddatum korrigieren oder offen lassen.");
                vorher.setGueltigBis(aktuell.getGueltigVon().minusDays(1));
            }
        }
        entity.getPreise().clear();
        entity.getPreise().addAll(preise);
    }

    private VerbrauchskostenDTO toDTO(Verbrauchskosten verbrauchskosten) {

        VerbrauchskostenDTO dto = new VerbrauchskostenDTO();

        dto.setId(verbrauchskosten.getId());
        dto.setMietobjektId(verbrauchskosten.getMietobjekt().getId());
        dto.setBezeichnung(verbrauchskosten.getBezeichnung());
        dto.setVerbrauchsart(verbrauchskosten.getVerbrauchsart());
        dto.setAbrechnungsart(verbrauchskosten.getAbrechnungsart());
        dto.setMengeneinheit(verbrauchskosten.getMengeneinheit());
        dto.setPreise(verbrauchskosten.getPreise().stream().map(p ->
                new com.kcserver.vermietung.entity.Verbrauchspreis(p.getGueltigVon(), p.getGueltigBis(), p.getPreis())).toList());
        dto.setPreis(verbrauchskosten.getPreise().stream()
                .filter(p -> !java.time.LocalDate.now(java.time.ZoneId.of("Europe/Berlin")).isBefore(p.getGueltigVon())
                        && (p.getGueltigBis() == null || !java.time.LocalDate.now(java.time.ZoneId.of("Europe/Berlin")).isAfter(p.getGueltigBis())))
                .map(com.kcserver.vermietung.entity.Verbrauchspreis::getPreis).findFirst().orElse(null));
        dto.setAktiv(verbrauchskosten.isAktiv());

        return dto;
    }
}