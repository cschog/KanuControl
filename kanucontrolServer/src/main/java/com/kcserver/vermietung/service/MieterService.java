package com.kcserver.vermietung.service;

import com.kcserver.core.entity.Person;
import com.kcserver.core.dto.person.PersonRefDTO;
import com.kcserver.core.mapper.PersonMapper;
import com.kcserver.kjfp.enumtype.CountryCode;
import com.kcserver.kjfp.repository.PersonRepository;
import com.kcserver.vermietung.dto.MieterDTO;
import com.kcserver.vermietung.dto.MieterSaveDTO;
import com.kcserver.vermietung.entity.Mieter;
import com.kcserver.vermietung.entity.Buchung;
import com.kcserver.vermietung.repository.BuchungRepository;
import com.kcserver.vermietung.repository.MieterRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@Transactional
public class MieterService {
    private final MieterRepository mieterRepository;
    private final PersonRepository personRepository;
    private final BuchungRepository buchungRepository;
    private final PersonMapper personMapper;

    public MieterService(MieterRepository mieterRepository, PersonRepository personRepository, BuchungRepository buchungRepository, PersonMapper personMapper) {
        this.mieterRepository = mieterRepository;
        this.personRepository = personRepository;
        this.buchungRepository = buchungRepository;
        this.personMapper = personMapper;
    }

    public List<MieterDTO> getAll() {
        Map<Long, List<Buchung>> buchungen = new HashMap<>();
        buchungRepository.findAll().forEach(b -> buchungen
                .computeIfAbsent(b.getMieter().getId(), ignored -> new ArrayList<>()).add(b));
        return mieterRepository.findAllByOrderByPersonNameAscPersonVornameAsc().stream()
                .map(m -> toDto(m, buchungen.getOrDefault(m.getPerson().getId(), List.of())))
                .toList();
    }

    public MieterDTO get(Long id) {
        Mieter mieter = find(id);
        return toDto(mieter, buchungenFuer(mieter.getPerson().getId()));
    }

    private List<Buchung> buchungenFuer(Long personId) {
        return buchungRepository.findAll().stream()
                .filter(b -> b.getMieter().getId().equals(personId)).toList();
    }

    public PersonRefDTO getPersonRef(Long personId) {
        Mieter mieter = mieterRepository.findByPersonId(personId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mieter nicht gefunden."));
        return personMapper.toPersonRefDTO(mieter.getPerson());
    }

    public MieterDTO create(MieterSaveDTO dto) {
        Person saved;
        if (dto.getPersonId() != null) {
            saved = personRepository.findLockedById(dto.getPersonId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "KJFP-Person nicht gefunden."));
            if (!saved.isKjfpSichtbar()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "KJFP-Person nicht gefunden.");
            if (mieterRepository.existsByPersonId(saved.getId())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Diese Person ist bereits als Mieter angelegt.");
            apply(saved, dto);
        } else {
            Person person = new Person();
            apply(person, dto);
            person.setSex(null);
            person.setKjfpSichtbar(false);
            person.setAktiv(true);
            saved = personRepository.save(person);
        }
        Mieter mieter = new Mieter();
        mieter.setPerson(saved);
        mieter.setAktiv(true);
        return toDto(mieterRepository.save(mieter), List.of());
    }

    public MieterDTO update(Long id, MieterSaveDTO dto) {
        Mieter mieter = findLocked(id);
        apply(mieter.getPerson(), dto);
        if (dto.getAktiv() != null) mieter.setAktiv(dto.getAktiv());
        return toDto(mieterRepository.save(mieter), buchungenFuer(mieter.getPerson().getId()));
    }

    public void deactivate(Long id) {
        Mieter mieter = findLocked(id);
        mieter.setAktiv(false);
    }

    public void delete(Long id) {
        Mieter mieter = findLocked(id);
        if (buchungRepository.existsByMieterId(mieter.getPerson().getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Der Mieter kann nicht gelöscht werden, solange noch Buchungen zugeordnet sind. "
                    + "Bitte zuerst alle zugehörigen Buchungen löschen. Das gilt auch für stornierte und abgeschlossene Buchungen.");
        }
        // Die Person kann zugleich in KJFP verwendet werden. Nur den Mietereintrag löschen.
        mieterRepository.delete(mieter);
        mieterRepository.flush();
    }

    private Mieter findLocked(Long id) {
        Mieter candidate = find(id);
        // Dieselbe Personensperre wie beim Anlegen/Ändern einer Buchung verhindert
        // eine neue Buchung zwischen Verwendungsprüfung und Löschen des Mieters.
        personRepository.findLockedById(candidate.getPerson().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mieter nicht gefunden."));
        return mieterRepository.findLockedById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mieter nicht gefunden."));
    }

    private Mieter find(Long id) {
        return mieterRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mieter nicht gefunden."));
    }

    private void apply(Person person, MieterSaveDTO dto) {
        person.setName(dto.getName().trim());
        person.setVorname(dto.getVorname().trim());
        person.setEmail(dto.getEmail());
        person.setTelefon(dto.getTelefon());
        person.setTelefonFestnetz(dto.getTelefonFestnetz());
        person.setStrasse(dto.getStrasse());
        person.setPlz(dto.getPlz());
        person.setOrt(dto.getOrt());
        person.setCountryCode(dto.getCountryCode() == null || dto.getCountryCode().isBlank() ? CountryCode.DE : CountryCode.valueOf(dto.getCountryCode()));
        person.setOrganisation(dto.getOrganisation());
        person.setKontoinhaber(dto.getKontoinhaber());
        person.setBankName(dto.getBankName());
        person.setIban(dto.getIban());
        person.setBic(dto.getBic());
    }

    private MieterDTO toDto(Mieter mieter, List<Buchung> buchungen) {
        Person p = mieter.getPerson();
        MieterDTO dto = new MieterDTO();
        dto.setId(mieter.getId());
        dto.setPersonId(p.getId());
        dto.setName(p.getName());
        dto.setVorname(p.getVorname());
        dto.setEmail(p.getEmail());
        dto.setTelefon(p.getTelefon());
        dto.setTelefonFestnetz(p.getTelefonFestnetz());
        dto.setStrasse(p.getStrasse());
        dto.setPlz(p.getPlz());
        dto.setOrt(p.getOrt());
        dto.setCountryCode(p.getCountryCode() == null ? "DE" : p.getCountryCode().name());
        dto.setOrganisation(p.getOrganisation());
        dto.setKontoinhaber(p.getKontoinhaber());
        dto.setBankName(p.getBankName());
        dto.setIban(p.getIban());
        dto.setBic(p.getBic());
        dto.setAktiv(mieter.isAktiv());
        dto.setBuchungsquellen(buchungen.stream().map(Buchung::getBuchungsquelle).distinct().toList());
        dto.setBuchungen(buchungen.size());
        dto.setMietobjekte(buchungen.stream().map(Buchung::getMietobjekt).filter(Objects::nonNull)
                .map(objekt -> objekt.getBezeichnung()).filter(name -> name != null && !name.isBlank())
                .map(String::trim).distinct().sorted(String.CASE_INSENSITIVE_ORDER).toList());
        return dto;
    }
}
