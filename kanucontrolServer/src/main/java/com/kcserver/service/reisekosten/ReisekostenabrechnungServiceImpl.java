package com.kcserver.service.reisekosten;

import com.kcserver.dto.person.PersonRefDTO;
import com.kcserver.dto.reisekosten.*;
import com.kcserver.entity.*;
import com.kcserver.mapper.PersonMapper;
import com.kcserver.repository.*;
import com.kcserver.service.AltersService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final AltersService altersService;
    private final TeilnehmerRepository teilnehmerRepository;
    private final FahrtabschnittMitfahrerRepository fahrtabschnittMitfahrerRepository;

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
    @Transactional(readOnly = true)
    public List<PersonRefDTO> getVerfuegbareReisekostenPersonen(
            Long veranstaltungId,
            String search
    ) {

        Veranstaltung veranstaltung =
                veranstaltungRepository.findById(
                        veranstaltungId
                ).orElseThrow();

        return teilnehmerRepository
                .searchRef(
                        veranstaltungId,
                        search
                )
                .stream()
                .map(Teilnehmer::getPerson)

                .filter(person -> {

                    Integer alter =
                            altersService.berechneAlterBeiBeginn(
                                    person.getGeburtsdatum(),
                                    veranstaltung.getBeginnDatum()
                            );

                    return alter != null
                            && alter >= 18;
                })

                .filter(person ->
                        !repository.isPersonBereitsFahrzeugZugeordnet(
                                veranstaltungId,
                                person.getId(),
                                null
                        )
                )

                .map(personMapper::toPersonRefDTO)

                .toList();
    }
    @Override
    @Transactional(readOnly = true)
    public List<PersonRefDTO> getVerfuegbareMitfahrer(
            Long veranstaltungId
    ) {

        return teilnehmerRepository
                .findAllWithPerson(veranstaltungId)
                .stream()
                .map(Teilnehmer::getPerson)

                .filter(person ->
                        !repository.isPersonBereitsFahrzeugZugeordnet(
                                veranstaltungId,
                                person.getId(),
                                null
                        )
                )

                .map(personMapper::toPersonRefDTO)

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

        validateFahrer(
                veranstaltung,
                fahrer
        );

        validatePersonNichtBereitsZugeordnet(
                veranstaltung.getId(),
                fahrer.getId(),
                -1L,
                fahrer.getVorname() + " " + fahrer.getName()
        );


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

                abrechnung.getMitfahrer()
                        .stream()
                        .map(person -> {
                            PersonRefDTO dto =
                                    personMapper.toPersonRefDTO(person);

                            dto.setVerwendetInFahrtabschnitten(
                                    fahrtabschnittMitfahrerRepository
                                            .existsByFahrtabschnittAbrechnungIdAndPersonId(
                                                    abrechnung.getId(),
                                                    person.getId()
                                            )
                            );

                            return dto;
                        })
                        .toList(),



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

        abrechnung.getMitfahrer().clear();

        if (request.mitfahrerIds() != null) {

            for (Long personId : request.mitfahrerIds()) {

                Person person = personRepository.findById(personId)
                        .orElseThrow();

                validateMitfahrer(
                        abrechnung.getVeranstaltung(),
                        abrechnung.getFahrer(),
                        person);

                validatePersonNichtBereitsZugeordnet(
                        abrechnung.getVeranstaltung().getId(),
                        person.getId(),
                        abrechnung.getId(),
                        person.getVorname() + " " + person.getName()
                );
                abrechnung.getMitfahrer().add(person);
            }
        }

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

                        validateMitfahrer(
                                abrechnung.getVeranstaltung(),
                                abrechnung.getFahrer(),
                                person);

                        validatePersonNichtBereitsZugeordnet(
                                abrechnung.getVeranstaltung().getId(),
                                person.getId(),
                                abrechnung.getId(),
                                person.getVorname() + " " + person.getName()
                        );

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
    public void delete(Long id) {

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

    private void validateFahrer(
            Veranstaltung veranstaltung,
            Person fahrer
    ) {

        boolean istTeilnehmer =
                teilnehmerRepository
                        .existsByVeranstaltungAndPerson(
                                veranstaltung,
                                fahrer
                        );

        if (!istTeilnehmer) {
            throw new IllegalArgumentException(
                    "Der Fahrer muss Teilnehmer der Veranstaltung sein."
            );
        }

        Integer alter =
                altersService.berechneAlterBeiBeginn(
                        fahrer.getGeburtsdatum(),
                        veranstaltung.getBeginnDatum()
                );

        if (alter == null) {
            throw new IllegalArgumentException(
                    "Beim Fahrer ist kein Geburtsdatum hinterlegt."
            );
        }

        if (alter < 18) {
            throw new IllegalArgumentException(
                    "Der Fahrer muss mindestens 18 Jahre alt sein."
            );
        }
    }


    private void validateMitfahrer(
            Veranstaltung veranstaltung,
            Person fahrer,
            Person mitfahrer
    ) {

        if (fahrer.getId().equals(
                mitfahrer.getId()
        )) {

            throw new IllegalArgumentException(
                    "Der Fahrer kann nicht gleichzeitig Mitfahrer sein."
            );
        }

        boolean istTeilnehmer =
                teilnehmerRepository
                        .existsByVeranstaltungAndPerson(
                                veranstaltung,
                                mitfahrer
                        );

        if (!istTeilnehmer) {
            throw new IllegalArgumentException(
                    "Mitfahrer muss Teilnehmer der Veranstaltung sein."
            );
        }
    }
    private void validatePersonNichtBereitsZugeordnet(
            Long veranstaltungId,
            Long personId,
            Long abrechnungId,
            String name
    ) {

        boolean bereitsZugeordnet =
                repository.isPersonBereitsFahrzeugZugeordnet(
                        veranstaltungId,
                        personId,
                        abrechnungId
                );

        if (bereitsZugeordnet) {
            throw new IllegalArgumentException(
                    name + " ist bereits einem Fahrzeug zugeordnet."
            );
        }
    }
}


