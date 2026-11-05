package com.kcserver.kjfp.service.teilnehmer;

import com.kcserver.core.entity.Person;
import com.kcserver.core.repository.MitgliedRepository;
import com.kcserver.kjfp.repository.PersonRepository;
import com.kcserver.core.dto.teilnehmer.TeilnehmerDetailDTO;
import com.kcserver.core.dto.teilnehmer.TeilnehmerKurzDTO;
import com.kcserver.core.dto.teilnehmer.TeilnehmerListDTO;


import com.kcserver.core.exception.ErrorMessages;
import com.kcserver.kjfp.dto.teilnehmer.*;
import com.kcserver.kjfp.dto.zahlungsnachweis.ZahlungsnachweisListDTO;
import com.kcserver.core.entity.Teilnehmer;
import com.kcserver.core.entity.Veranstaltung;
import com.kcserver.kjfp.entity.beitraege.ZahlungsPosition;
import com.kcserver.kjfp.entity.beitraege.Zahlungsnachweis;
import com.kcserver.core.enumtype.TeilnehmerRolle;
import com.kcserver.kjfp.enumtype.Zahlungsstatus;
import com.kcserver.core.mapper.TeilnehmerMapper;

import com.kcserver.core.repository.VeranstaltungRepository;
import com.kcserver.kjfp.service.beitrag.TeilnehmerBeitragService;
import com.kcserver.kjfp.service.zahlungsnachweis.ZahlungsstatusService;
import com.kcserver.core.repository.TeilnehmerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import com.kcserver.kjfp.repository.zahlungsnachweis.ZahlungsnachweisRepository;
import com.kcserver.kjfp.repository.abrechnung.AbrechnungBuchungRepository;


import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.kcserver.core.exception.EntityFinder.getOr404;


@Service
@RequiredArgsConstructor
@Transactional
public class KjfpTeilnehmerService {
    private final PersonRepository personRepository;
    private final MitgliedRepository mitgliedRepository;
    private final TeilnehmerRepository teilnehmerRepository;
    private final VeranstaltungRepository veranstaltungRepository;
    private final TeilnehmerMapper teilnehmerMapper;
    private final TeilnehmerBeitragService teilnehmerBeitragService;
    private final ZahlungsstatusService zahlungsstatusService;
    private final ZahlungsnachweisRepository zahlungsnachweisRepository;
    private final AbrechnungBuchungRepository abrechnungBuchungRepository;

    public TeilnehmerDetailDTO setKjfpZuordnung(Long veranstaltungId, Long teilnehmerId, boolean zugeordnet) {
        Teilnehmer teilnehmer = teilnehmerRepository.findByIdAndVeranstaltung_Id(teilnehmerId, veranstaltungId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.TEILNEHMER_IN_VERANSTALTUNG_NOT_FOUND));
        if (zugeordnet && !teilnehmer.getVeranstaltung().isKjfpAktiv()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "KJFP-Förderverwaltung ist für diese Veranstaltung nicht aktiviert.");
        }
        if (!zugeordnet && teilnehmer.getVeranstaltung().isKjfpAktiv() && teilnehmer.getRolle() == TeilnehmerRolle.LEITER) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Die KJFP-Veranstaltungsleitung muss der KJFP-Fachverwaltung zugeordnet bleiben.");
        }
        if (!zugeordnet && (abrechnungBuchungRepository.existsByTeilnehmer_Id(teilnehmerId)
                || zahlungsnachweisRepository.existsByPositionenTeilnehmerId(teilnehmerId))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Die KJFP-Zuordnung kann nicht entfernt werden, weil bereits Abrechnungsdaten für den Teilnehmer bestehen.");
        }
        teilnehmer.setKjfpZugeordnet(zugeordnet);
        return teilnehmerMapper.toDetailDTO(teilnehmer);
    }

    public TeilnehmerBeitraegeResponseDTO findAllByVeranstaltungForBeitraege(
            Long veranstaltungId
    ) {

        Veranstaltung veranstaltung =
                veranstaltungRepository
                        .findById(veranstaltungId)
                        .orElseThrow();

        List<Teilnehmer> teilnehmer =
                teilnehmerRepository.findAllWithPerson(
                        veranstaltungId
                );

        Map<Long, BigDecimal> gezahlteBetraege =
                zahlungsstatusService.getGezahlteBetraege(veranstaltungId);

        List<TeilnehmerListDTO> teilnehmerDTOs =
                teilnehmer.stream()
                        .map(t -> {

                            TeilnehmerListDTO dto =
                                    teilnehmerMapper.toListDTO(t);

                            BigDecimal sollBeitrag =
                                    teilnehmerBeitragService.getSollBeitrag(
                                            veranstaltung,
                                            t
                                    );

                            BigDecimal gezahlterBetrag =
                                    gezahlteBetraege.getOrDefault(
                                            t.getId(),
                                            BigDecimal.ZERO
                                    );

                            dto.setSollBeitrag(sollBeitrag);
                            dto.setGezahlterBetrag(gezahlterBetrag);
                            dto.setZahlungsstatus(
                                    zahlungsstatusService.getStatus(
                                            sollBeitrag,
                                            gezahlterBetrag
                                    )
                            );

                            return dto;
                        })
                        .toList();
        TeilnehmerBeitraegeResponseDTO response =
                new TeilnehmerBeitraegeResponseDTO();

        response.setTeilnehmer(teilnehmerDTOs);

/* =========================================================
   ZAHLUNGSNACHWEISE
   ========================================================= */

        List<ZahlungsnachweisListDTO> zahlungsnachweise =
                zahlungsnachweisRepository.findListByVeranstaltungId(
                        veranstaltungId
                );

        List<Zahlungsnachweis> zahlungsnachweisDetails =
                zahlungsnachweisRepository.findDetailsByVeranstaltungId(
                        veranstaltungId
                );

        Map<Long, Zahlungsnachweis> nachweisMap =
                zahlungsnachweisDetails.stream()
                        .collect(Collectors.toMap(
                                Zahlungsnachweis::getId,
                                Function.identity()
                        ));

        for (ZahlungsnachweisListDTO dto : zahlungsnachweise) {

            Zahlungsnachweis nachweis =
                    nachweisMap.get(dto.getId());

            if (nachweis != null) {
                dto.setTeilnehmer(
                        getTeilnehmerKurzDTOs(nachweis)
                );
            }
        }

        response.setZahlungsnachweise(zahlungsnachweise);

        TeilnehmerBeitragSummaryDTO summary =
                new TeilnehmerBeitragSummaryDTO();

        BigDecimal sollSumme =
                teilnehmerDTOs.stream()
                        .map(TeilnehmerListDTO::getSollBeitrag)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal bezahltSumme =
                teilnehmerDTOs.stream()
                        .map(TeilnehmerListDTO::getGezahlterBetrag)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal offenSumme =
                sollSumme.subtract(bezahltSumme).max(BigDecimal.ZERO);

        long bezahlt =
                teilnehmerDTOs.stream()
                        .filter(t -> t.getZahlungsstatus() == Zahlungsstatus.GRUEN)
                        .count();

        long teilweise =
                teilnehmerDTOs.stream()
                        .filter(t -> t.getZahlungsstatus() == Zahlungsstatus.GELB)
                        .count();

        long offen =
                teilnehmerDTOs.stream()
                        .filter(t -> t.getZahlungsstatus() == Zahlungsstatus.ROT)
                        .count();

        summary.setAnzahlTeilnehmer(teilnehmerDTOs.size());
        summary.setBezahlt((int) bezahlt);
        summary.setTeilweise((int) teilweise);
        summary.setOffen((int) offen);

        summary.setSollSumme(sollSumme);
        summary.setBezahltSumme(bezahltSumme);
        summary.setOffenSumme(offenSumme);

        response.setSummary(summary);

        return response;
    }

    private List<TeilnehmerKurzDTO> getTeilnehmerKurzDTOs(
            Zahlungsnachweis zahlungsnachweis
    ) {
        return zahlungsnachweis.getPositionen()
                .stream()
                .map(ZahlungsPosition::getTeilnehmer)
                .filter(Objects::nonNull)
                .filter(t -> t.getPerson() != null)
                .map(teilnehmer -> new TeilnehmerKurzDTO(
                        teilnehmer.getId(),
                        teilnehmer.getPerson().getId(),
                        teilnehmer.getPerson().getVorname(),
                        teilnehmer.getPerson().getName()
                ))
                .toList();
    }

    public TeilnehmerDetailDTO addTeilnehmer(Long veranstaltungId, Long personId) {

        Veranstaltung veranstaltung = veranstaltungRepository.findByIdWithRelations(veranstaltungId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        ErrorMessages.VERANSTALTUNG_NOT_FOUND
                ));

        if (!veranstaltung.isKjfpAktiv()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "KJFP-Förderverwaltung ist für diese Veranstaltung nicht aktiviert.");
        }

        Person person = getPerson(personId);

        // ❗ Duplicate verhindern → 409
        if (teilnehmerRepository
                .findByVeranstaltungAndPerson(veranstaltung, person)
                .isPresent()) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    ErrorMessages.TEILNEHMER_ALREADY_EXISTS
            );
        }

        Teilnehmer teilnehmer = new Teilnehmer();
        teilnehmer.setVeranstaltung(veranstaltung);
        teilnehmer.setPerson(person);
        teilnehmer.setKjfpZugeordnet(true);

// ⭐ Neue Logik
        boolean hasFunktion = mitgliedRepository
                .existsByPerson_IdAndFunktionIsNotNull(person.getId());

        if (hasFunktion) {
            teilnehmer.setRolle(TeilnehmerRolle.MITARBEITER);
        } else {
            teilnehmer.setRolle(null);
        }

        return teilnehmerMapper.toDetailDTO(
                teilnehmerRepository.save(teilnehmer)
        );
    }

    public void addTeilnehmerBulk(Long veranstaltungId, List<Long> personIds) {

        Veranstaltung veranstaltung = veranstaltungRepository.findByIdWithRelations(veranstaltungId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, ErrorMessages.VERANSTALTUNG_NOT_FOUND
                ));

        if (!veranstaltung.isKjfpAktiv()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "KJFP-Förderverwaltung ist für diese Veranstaltung nicht aktiviert.");
        }

        for (Long personId : personIds) {

            Person person = getPerson(personId);

            boolean exists = teilnehmerRepository
                    .findByVeranstaltungAndPerson(veranstaltung, person)
                    .isPresent();

            if (!exists) {
                Teilnehmer t = new Teilnehmer();
                t.setVeranstaltung(veranstaltung);
                t.setPerson(person);
                boolean hasFunktion = mitgliedRepository
                        .existsByPerson_IdAndFunktionIsNotNull(person.getId());

                t.setRolle(hasFunktion ? TeilnehmerRolle.MITARBEITER : null);
                teilnehmerRepository.save(t);
            }
        }
    }

    private Person getPerson(Long id) {
        return personRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, ErrorMessages.PERSON_NOT_FOUND
                ));
    }
}
