package com.kcserver.service;

import com.kcserver.dto.beitrag.BeitragsregelCreateDTO;
import com.kcserver.dto.beitrag.BeitragsstrukturDTO;
import com.kcserver.dto.beitrag.BeitragsstrukturUpdateDTO;
import com.kcserver.entity.Beitragsregel;
import com.kcserver.entity.Beitragsstruktur;
import com.kcserver.enumtype.TeilnehmerRolle;
import com.kcserver.mapper.BeitragsstrukturMapper;
import com.kcserver.repository.BeitragsregelRepository;
import com.kcserver.repository.BeitragsstrukturRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BeitragsstrukturService {

    private final BeitragsstrukturRepository repository;
    private final BeitragsregelValidator validator;
    private final BeitragsstrukturMapper mapper;
    private final BeitragsregelRepository regelRepository;

    @Transactional
    public BeitragsstrukturDTO createTemplate(BeitragsstrukturDTO dto) {

        Beitragsstruktur s = new Beitragsstruktur();

        s.setName(dto.getName());
        s.setTemplate(true);
        s.setAktiv(true);
        s.setSystem(false);

        Beitragsstruktur saved = repository.save(s);

        return mapper.toDTO(saved);
    }

    @Transactional
    public BeitragsstrukturDTO update(
            Long id,
            BeitragsstrukturUpdateDTO dto
    ) {

        Beitragsstruktur struktur =
                repository.findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND
                                )
                        );

        if (dto.getName() != null) {
            struktur.setName(dto.getName());
        }

        return mapper.toDTO(
                repository.save(struktur)
        );
    }

    @Transactional
    public void delete(Long id) {

        Beitragsstruktur struktur =
                repository.findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND
                                )
                        );

        repository.delete(struktur);
    }

    @Transactional
    public BeitragsstrukturDTO updateRegel(
            Long regelId,
            BeitragsregelCreateDTO dto
    ) {

        Beitragsregel regel =
                regelRepository.findById(regelId)
                        .orElseThrow();

        regel.setAlterVon(dto.getAlterVon());
        regel.setAlterBis(dto.getAlterBis());
        regel.setRolle(dto.getRolle());
        regel.setBeitrag(dto.getBeitrag());

        Beitragsstruktur struktur =
                regel.getBeitragsstruktur();

        validator.validate(struktur.getRegeln());

        return mapper.toDTO(struktur);
    }

    @Transactional
    public void deleteRegel(Long regelId) {

        Beitragsregel regel =
                regelRepository.findById(regelId)
                        .orElseThrow();

        Beitragsstruktur struktur =
                regel.getBeitragsstruktur();

        struktur.getRegeln().remove(regel);

        regelRepository.delete(regel);

        validator.validate(struktur.getRegeln());
    }

    @Transactional
    public Beitragsstruktur copyFromTemplateEntity(
            Long templateId,
            String neuerName
    ) {

        Beitragsstruktur template =
                repository.findById(templateId)
                        .orElseThrow(() -> new IllegalArgumentException("Template nicht gefunden"));

        if (!template.isTemplate()) {
            throw new IllegalStateException("Keine Template-Struktur");
        }

        Beitragsstruktur copy = new Beitragsstruktur();

        copy.setName(neuerName);
        copy.setAktiv(true);
        copy.setTemplate(false);
        copy.setSystem(false);

        List<Beitragsregel> neueRegeln =
                template.getRegeln()
                        .stream()
                        .map(r -> {
                            Beitragsregel nr = new Beitragsregel();

                            nr.setBeitragsstruktur(copy);
                            nr.setAlterVon(r.getAlterVon());
                            nr.setAlterBis(r.getAlterBis());
                            nr.setRolle(r.getRolle());
                            nr.setBeitrag(r.getBeitrag());

                            return nr;
                        })
                        .toList();

        copy.setRegeln(new ArrayList<>(neueRegeln));

        validator.validate(copy.getRegeln());

        return repository.save(copy);
    }

    @Transactional
    public BeitragsstrukturDTO copyFromTemplate(
            Long templateId,
            String neuerName
    ) {
        return mapper.toDTO(
                copyFromTemplateEntity(templateId, neuerName)
        );
    }

    @Transactional(readOnly = true)
    public List<BeitragsstrukturDTO> getAll() {

        return repository.findByTemplateFalse()
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Transactional
    public List<BeitragsstrukturDTO> getTemplates() {

        List<Beitragsstruktur> templates =
                repository.findByTemplateTrue();

        if (templates.isEmpty()) {
            createDefaultTemplates();   // 🔥 HIER
            templates = repository.findByTemplateTrue();
        }

        return templates.stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Transactional
    public BeitragsstrukturDTO createFromDefaultTemplate(
            String name
    ) {

        Beitragsstruktur template =
                repository.findFirstByTemplateTrue()
                        .orElseThrow();

        Beitragsstruktur copy =
                copyFromTemplateEntity(
                        template.getId(),
                        name
                );

        return mapper.toDTO(copy);
    }

    @Transactional
    protected void createDefaultTemplates() {

        Beitragsstruktur struktur = new Beitragsstruktur();

        struktur.setName("Standard Kinder/Jugend");
        struktur.setTemplate(true);
        struktur.setAktiv(true);
        struktur.setSystem(true);

        Beitragsregel r0 = new Beitragsregel();
        r0.setAlterVon(0);
        r0.setAlterBis(2);
        r0.setBeitrag(BigDecimal.ZERO);
        r0.setBeitragsstruktur(struktur);

        Beitragsregel r1 = new Beitragsregel();
        r1.setAlterVon(3);
        r1.setAlterBis(10);
        r1.setBeitrag(BigDecimal.valueOf(100));
        r1.setBeitragsstruktur(struktur);

        Beitragsregel r2 = new Beitragsregel();
        r2.setAlterVon(11);
        r2.setAlterBis(20);
        r2.setBeitrag(BigDecimal.valueOf(200));
        r2.setBeitragsstruktur(struktur);

        Beitragsregel r3 = new Beitragsregel();
        r3.setAlterVon(21);
        r3.setAlterBis(26);
        r3.setBeitrag(BigDecimal.valueOf(300));
        r3.setBeitragsstruktur(struktur);

        Beitragsregel r4 = new Beitragsregel();
        r4.setAlterVon(27);
        r4.setAlterBis(99);
        r4.setBeitrag(BigDecimal.valueOf(400));
        r4.setBeitragsstruktur(struktur);

        Beitragsregel m = new Beitragsregel();
        m.setAlterVon(0);
        m.setAlterBis(99);
        m.setRolle(TeilnehmerRolle.MITARBEITER);
        m.setBeitrag(BigDecimal.ZERO);
        m.setBeitragsstruktur(struktur);

        Beitragsregel l = new Beitragsregel();
        l.setAlterVon(0);
        l.setAlterBis(99);
        l.setRolle(TeilnehmerRolle.LEITER);
        l.setBeitrag(BigDecimal.ZERO);
        l.setBeitragsstruktur(struktur);

        struktur.setRegeln(List.of(r0, r1, r2, r3, r4, m, l));

        repository.save(struktur);
    }

    @Transactional
    public BeitragsstrukturDTO addRegel(
            Long strukturId,
            BeitragsregelCreateDTO dto
    ) {

        Beitragsstruktur struktur = repository.findById(strukturId)
                .orElseThrow(() -> new IllegalArgumentException("Struktur nicht gefunden"));

        Beitragsregel regel = new Beitragsregel();

        regel.setBeitragsstruktur(struktur);
        regel.setAlterVon(dto.getAlterVon());
        regel.setAlterBis(dto.getAlterBis());
        regel.setRolle(dto.getRolle());
        regel.setBeitrag(dto.getBeitrag());

        struktur.getRegeln().add(regel);

        // 🔥 Wichtig: validieren (dein bestehender Validator)
        validator.validate(struktur.getRegeln());

        Beitragsstruktur saved = repository.save(struktur);

        return mapper.toDTO(saved);
    }
}