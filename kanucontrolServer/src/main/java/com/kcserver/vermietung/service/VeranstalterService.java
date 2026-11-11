package com.kcserver.vermietung.service;

import com.kcserver.core.entity.Verein;
import com.kcserver.core.repository.VereinRepository;
import com.kcserver.core.dto.person.PersonRefDTO;
import com.kcserver.kjfp.repository.PersonRepository;
import com.kcserver.kjfp.enumtype.CountryCode;
import com.kcserver.vermietung.dto.VeranstalterDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Locale;

@Service @RequiredArgsConstructor @Transactional
public class VeranstalterService {
    private final VereinRepository vereine;
    private final PersonRepository personen;
    private final com.kcserver.vermietung.repository.BuchungRepository buchungen;

    @Transactional(readOnly=true)
    public List<VeranstalterDTO> suchen(String search) {
        VorlagenService.tenant();
        String term=search==null?"":search.strip().toLowerCase(Locale.ROOT);
        var anzahl = buchungen.countNachVeranstalter().stream().collect(java.util.stream.Collectors.toMap(
            com.kcserver.vermietung.repository.BuchungRepository.VeranstalterBuchungszahl::getVeranstalterId,
            com.kcserver.vermietung.repository.BuchungRepository.VeranstalterBuchungszahl::getAnzahl));
        return vereine.findAll((root,query,cb)->cb.or(
            cb.like(cb.lower(root.get("name")),"%"+term+"%"),
            cb.like(cb.lower(root.get("abk")),"%"+term+"%")))
            .stream().map(v -> {var d=dto(v);d.setBuchungen(anzahl.getOrDefault(v.getId(),0L));return d;}).sorted(java.util.Comparator.comparing(VeranstalterDTO::getName)).toList();
    }
    @Transactional(readOnly=true)
    public VeranstalterDTO lesen(Long id) { VorlagenService.tenant(); return dtoMitBuchungen(organisation(id)); }
    public VeranstalterDTO speichern(Long id, VeranstalterDTO d) {
        VorlagenService.tenant();
        Verein v=id==null?new Verein():organisation(id);
        if(id==null)v.setKjfpVerein(false);
        String name=d.getName().strip(), abk=d.getAbk().strip();
        vereine.findByAbkAndName(abk,name).ifPresent(other->{
            if(!other.getId().equals(id))throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Diese Organisation existiert bereits. Bitte den vorhandenen Datensatz auswählen.");
        });
        CountryCode country;
        try { country=CountryCode.valueOf(d.getCountryCode()); }
        catch(IllegalArgumentException ex) { throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"Ungültiges Land."); }
        v.setName(name);v.setAbk(abk);v.setStrasse(d.getStrasse());v.setPlz(d.getPlz());v.setOrt(d.getOrt());v.setCountryCode(country);
        v.setBankName(d.getBankName());v.setIban(d.getIban());v.setBic(d.getBic());
        v.setKontoinhaber(d.getKontoinhaberId()==null?null:personen.findById(d.getKontoinhaberId()).orElseThrow(()->
            new ResponseStatusException(HttpStatus.NOT_FOUND,"Kontoinhaber nicht gefunden.")));
        // Deliberate whitelist: telephone, protection concept, KiK, house club and KJFP usage survive unchanged.
        return dtoMitBuchungen(vereine.save(v));
    }
    private Verein organisation(Long id) {
        return vereine.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Veranstalter nicht gefunden."));
    }
    private VeranstalterDTO dtoMitBuchungen(Verein v) {
        var d=dto(v);d.setBuchungen(v.getId()==null?0:buchungen.countByVeranstalterId(v.getId()));return d;
    }
    private VeranstalterDTO dto(Verein v) {
        var d=new VeranstalterDTO();d.setId(v.getId());d.setName(v.getName());d.setAbk(v.getAbk());
        d.setStrasse(v.getStrasse());d.setPlz(v.getPlz());d.setOrt(v.getOrt());d.setCountryCode(v.getCountryCode().name());
        d.setBankName(v.getBankName());d.setIban(v.getIban());d.setBic(v.getBic());
        if(v.getKontoinhaber()!=null){var p=v.getKontoinhaber();var ref=new PersonRefDTO();ref.setId(p.getId());ref.setName(p.getName());ref.setVorname(p.getVorname());d.setKontoinhaber(ref);d.setKontoinhaberId(p.getId());}
        return d;
    }
}
