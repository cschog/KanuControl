package com.kcserver.service.reisekosten;

import com.kcserver.dto.reisekosten.*;
import com.kcserver.entity.*;
import com.kcserver.mapper.PersonMapper;
import com.kcserver.repository.ReisekostenabrechnungRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kcserver.repository.PersonRepository;
import com.kcserver.repository.VeranstaltungRepository;
import com.kcserver.repository.ReisekostenKonfigurationRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class ReisekostenabrechnungServiceImpl
        implements ReisekostenabrechnungService {

    private final ReisekostenabrechnungRepository repository;
    private final VeranstaltungRepository veranstaltungRepository;
    private final PersonRepository personRepository;
    private final PersonMapper personMapper;
    private final ReisekostenKonfigurationRepository
            konfigurationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ReisekostenabrechnungListResponse> listByVeranstaltung(
            Long veranstaltungId
    ) {

        return repository.findByVeranstaltungId(veranstaltungId)
                .stream()
                .map(r -> new ReisekostenabrechnungListResponse(
                        r.getId(),
                        r.getFahrer().getId(),
                        r.getFahrer().getVorname() + " " + r.getFahrer().getName(),
                        r.getGesamtKilometer(),
                        r.getGesamtBetrag()
                ))
                .toList();
    }

    @Override

    public Long create(
            ReisekostenabrechnungCreateRequest request
    ) {
        Veranstaltung veranstaltung =
                veranstaltungRepository.findById(
                        request.veranstaltungId()
                ).orElseThrow();
        Person fahrer =
                personRepository.findById(
                        request.fahrerId()

                ).orElseThrow();

        Reisekostenabrechnung abrechnung =

                new Reisekostenabrechnung();

        abrechnung.setVeranstaltung(veranstaltung);
        abrechnung.setFahrer(fahrer);
        abrechnung.setAbrechnungsdatum(
                request.abrechnungsdatum()
        );
        abrechnung.setBemerkung(
                request.bemerkung()
        );
        abrechnung.setGesamtKilometer(0);
        abrechnung.setGesamtBetrag(BigDecimal.ZERO);

        repository.save(abrechnung);
        return abrechnung.getId();

    }

    @Override
    @Transactional(readOnly = true)
    public ReisekostenabrechnungDetailResponse get(
            Long id
    ) {

        Reisekostenabrechnung abrechnung =
                repository.findById(id)
                        .orElseThrow();

        return new ReisekostenabrechnungDetailResponse(
                abrechnung.getId(),
                abrechnung.getVeranstaltung().getId(),
                abrechnung.getVeranstaltung().getName(),
                abrechnung.getFahrer().getId(),
                abrechnung.getFahrer().getVorname()
                        + " "
                        + abrechnung.getFahrer().getName(),
                abrechnung.getAbrechnungsdatum(),
                abrechnung.getGesamtKilometer(),
                abrechnung.getGesamtBetrag(),
                abrechnung.getBemerkung(),
                abrechnung.getFahrtabschnitte()
                        .stream()
                        .map(abschnitt ->
                                new FahrtabschnittResponse(
                                        abschnitt.getId(),
                                        abschnitt.getReihenfolge(),
                                        abschnitt.getBeschreibung(),
                                        abschnitt.getVonOrt(),
                                        abschnitt.getNachOrt(),
                                        abschnitt.getKilometer(),
                                        abschnitt.isAnhaenger(),
                                        abschnitt.getMitfahrer()
                                                .stream()
                                                .map(FahrtabschnittMitfahrer::getPerson)
                                                .map(personMapper::toPersonRefDTO)
                                                .toList()
                                )
                        )
                        .toList()
        );
    }

    @Override
    public void update(
            Long id,
            ReisekostenabrechnungUpdateRequest request
    ) {

        Reisekostenabrechnung abrechnung =
                repository.findById(id)
                        .orElseThrow();
        abrechnung.setAbrechnungsdatum(
                request.abrechnungsdatum()
        );
        abrechnung.setBemerkung(
                request.bemerkung()
        );

        abrechnung.getFahrtabschnitte().clear();
        int gesamtKilometer = 0;

        if (request.fahrtabschnitte() != null) {

            for (FahrtabschnittRequest dto : request.fahrtabschnitte()) {
                Fahrtabschnitt abschnitt = getFahrtabschnitt(dto, abrechnung);
                if (dto.mitfahrerIds() != null) {
                    for (Long personId : dto.mitfahrerIds()) {
                        Person person =
                                personRepository.findById(personId)
                                        .orElseThrow();
                        FahrtabschnittMitfahrer mitfahrer =
                                new FahrtabschnittMitfahrer();

                        mitfahrer.setFahrtabschnitt(abschnitt);
                        mitfahrer.setPerson(person);

                        abschnitt.getMitfahrer().add(mitfahrer);
                    }
                }
                abrechnung.getFahrtabschnitte().add(abschnitt);
                gesamtKilometer +=
                        dto.kilometer() == null
                                ? 0
                                : dto.kilometer();
            }
        }
        abrechnung.setGesamtKilometer(
                gesamtKilometer
        );
        abrechnung.setGesamtBetrag(
                berechneGesamtbetrag(abrechnung)
        );
    }

    private @NonNull Fahrtabschnitt getFahrtabschnitt(FahrtabschnittRequest dto, Reisekostenabrechnung abrechnung) {
        Fahrtabschnitt abschnitt =
                new Fahrtabschnitt();

        abschnitt.setAbrechnung(abrechnung);

        abschnitt.setReihenfolge(
                dto.reihenfolge()
        );

        abschnitt.setBeschreibung(
                dto.beschreibung()
        );

        abschnitt.setVonPlz(
                dto.vonPlz()
        );

        abschnitt.setVonOrt(
                dto.vonOrt()
        );

        abschnitt.setVonCountryCode(
                dto.vonCountryCode()
        );

        abschnitt.setNachPlz(
                dto.nachPlz()
        );

        abschnitt.setNachOrt(
                dto.nachOrt()
        );

        abschnitt.setNachCountryCode(
                dto.nachCountryCode()
        );

        abschnitt.setKilometer(
                dto.kilometer()
        );

        abschnitt.setAnhaenger(
                dto.anhaenger()
        );
        return abschnitt;
    }

    @Override
    public void delete(
            Long id
    ) {
        repository.deleteById(id);
    }

    private ReisekostenKonfiguration getKonfiguration(
            LocalDate datum
    ) {

        return konfigurationRepository
                .findFirstByGueltigVonLessThanEqualAndGueltigBisGreaterThanEqual(
                        datum,
                        datum
                )
                .or(() ->
                        konfigurationRepository
                                .findFirstByGueltigVonLessThanEqualAndGueltigBisIsNull(
                                        datum
                                )
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Keine Reisekostenkonfiguration für "
                                        + datum
                                        + " gefunden"
                        )
                );
    }

    private BigDecimal berechneGesamtbetrag(
            Reisekostenabrechnung abrechnung
    ) {
        LocalDate datum =
                abrechnung.getAbrechnungsdatum();

        if (datum == null) {
            datum = LocalDate.now();
        }

        ReisekostenKonfiguration config =
                getKonfiguration(datum);

        BigDecimal gesamt = BigDecimal.ZERO;

        for (Fahrtabschnitt abschnitt :
                abrechnung.getFahrtabschnitte()) {

            if (abschnitt.getKilometer() == null) {
                continue;
            }

            BigDecimal kilometer =
                    BigDecimal.valueOf(
                            abschnitt.getKilometer()
                    );

            BigDecimal betrag =
                    kilometer.multiply(
                            config.getPkwSatz()
                    );

            if (abschnitt.isAnhaenger()) {

                betrag = betrag.add(
                        kilometer.multiply(
                                config.getAnhaengerSatz()
                        )
                );
            }

            int anzahlMitfahrer =
                    abschnitt.getMitfahrer().size();

            if (anzahlMitfahrer > 0) {

                betrag = betrag.add(
                        kilometer
                                .multiply(
                                        config.getMitfahrerSatz()
                                )
                                .multiply(
                                        BigDecimal.valueOf(
                                                anzahlMitfahrer
                                        )
                                )
                );
            }

            gesamt = gesamt.add(betrag);
        }

        return gesamt.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }
}


