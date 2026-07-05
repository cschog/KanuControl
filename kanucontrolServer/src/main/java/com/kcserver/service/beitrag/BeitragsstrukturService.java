package com.kcserver.service.beitrag;

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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static com.kcserver.exception.ErrorMessages.BEITRAGSSTRUKTUR_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class BeitragsstrukturService {

    private final BeitragsstrukturRepository repository;
    private final BeitragsregelRepository regelRepository;
    private final BeitragsregelValidator validator;
    private final BeitragsstrukturMapper mapper;

    /* =========================================================
       CREATE TEMPLATE
       ========================================================= */

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

    /* =========================================================
       UPDATE STRUKTUR
       ========================================================= */

    @Transactional
    public BeitragsstrukturDTO update(
            Long id,
            BeitragsstrukturUpdateDTO dto
    ) {



        Beitragsstruktur struktur =
                repository.findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        BEITRAGSSTRUKTUR_NOT_FOUND
                                )
                        );

        if (dto.getName() != null) {
            struktur.setName(dto.getName());
        }

        return mapper.toDTO(
                repository.save(struktur)
        );
    }

    /* =========================================================
       UPDATE REGELN
       ========================================================= */

    @Transactional
    public BeitragsstrukturDTO updateRegeln(
            Long strukturId,
            List<BeitragsregelCreateDTO> dto
    ) {

        Beitragsstruktur struktur =
                repository.findById(strukturId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Beitragsstruktur nicht gefunden"
                                )
                        );

        regelRepository.deleteAll(struktur.getRegeln());

        struktur.getRegeln().clear();

        Map<TeilnehmerRolle, List<BeitragsregelCreateDTO>> gruppiert =
                dto.stream()
                        .collect(Collectors.groupingBy(
                                BeitragsregelCreateDTO::getRolle
                        ));

        List<Beitragsregel> neueRegeln = new ArrayList<>();

        for (List<BeitragsregelCreateDTO> gruppe : gruppiert.values()) {

            gruppe.sort(
                    Comparator.comparing(r ->
                            r.getAlterBis() == null
                                    ? Integer.MAX_VALUE
                                    : r.getAlterBis()
                    )
            );

            int sortierung = 0;

            for (BeitragsregelCreateDTO r : gruppe) {

                Beitragsregel entity = new Beitragsregel();

                entity.setStruktur(struktur);

                entity.setSortierung(sortierung++);

                entity.setRolle(r.getRolle());

                entity.setAlterBis(r.getAlterBis());

                entity.setBeitrag(r.getBeitrag());

                neueRegeln.add(entity);
            }
        }

        validator.validate(neueRegeln);

        regelRepository.saveAll(neueRegeln);

        struktur.setRegeln(neueRegeln);

        return mapper.toDTO(struktur);
    }

    /* =========================================================
       DELETE STRUKTUR
       ========================================================= */

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

    /* =========================================================
       UPDATE REGEL
       ========================================================= */

    @Transactional
    public BeitragsstrukturDTO updateRegel(
            Long regelId,
            BeitragsregelCreateDTO dto
    ) {

        Beitragsregel regel =
                regelRepository.findById(regelId)
                        .orElseThrow();

        regel.setAlterBis(dto.getAlterBis());

        regel.setRolle(dto.getRolle());

        regel.setBeitrag(dto.getBeitrag());

        Beitragsstruktur struktur =
                regel.getStruktur();

        validator.validate(struktur.getRegeln());

        return mapper.toDTO(struktur);
    }

    /* =========================================================
       DELETE REGEL
       ========================================================= */

    @Transactional
    public void deleteRegel(Long regelId) {

        Beitragsregel regel =
                regelRepository.findById(regelId)
                        .orElseThrow();

        Beitragsstruktur struktur =
                regel.getStruktur();

        struktur.getRegeln().remove(regel);

        regelRepository.delete(regel);

        validator.validate(struktur.getRegeln());
    }

    @Transactional
    public BeitragsstrukturDTO copy(
            Long strukturId,
            String neuerName
    ) {

        Beitragsstruktur original =
                repository.findById(strukturId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Beitragsstruktur nicht gefunden"
                                )
                        );

        Beitragsstruktur copy = new Beitragsstruktur();

        copy.setName(neuerName);
        copy.setTemplate(false);
        copy.setAktiv(true);
        copy.setSystem(false);

        List<Beitragsregel> neueRegeln =
                original.getRegeln()
                        .stream()
                        .map(r -> {

                            Beitragsregel nr = new Beitragsregel();

                            nr.setStruktur(copy);

                            nr.setSortierung(r.getSortierung());
                            nr.setAlterBis(r.getAlterBis());
                            nr.setRolle(r.getRolle());
                            nr.setBeitrag(r.getBeitrag());

                            return nr;
                        })
                        .toList();

        copy.setRegeln(neueRegeln);

        validator.validate(copy.getRegeln());

        Beitragsstruktur saved =
                repository.save(copy);

        return mapper.toDTO(saved);
    }

    /* =========================================================
       COPY TEMPLATE ENTITY
       ========================================================= */

    @Transactional
    public Beitragsstruktur copyFromTemplateEntity(
            Long templateId,
            String neuerName
    ) {

        Beitragsstruktur template =
                repository.findById(templateId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Template nicht gefunden"
                                )
                        );

        if (!template.isTemplate()) {
            throw new IllegalStateException(
                    "Keine Template-Struktur"
            );
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

                            nr.setStruktur(copy);

                            nr.setSortierung(r.getSortierung());

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

    /* =========================================================
       COPY TEMPLATE
       ========================================================= */

    @Transactional
    public BeitragsstrukturDTO copyFromTemplate(
            Long templateId,
            String neuerName
    ) {

        return mapper.toDTO(
                copyFromTemplateEntity(
                        templateId,
                        neuerName
                )
        );
    }

    /* =========================================================
       GET ALL
       ========================================================= */

    @Transactional(readOnly = true)
    public List<BeitragsstrukturDTO> getAll() {

        return repository.findByTemplateFalse()
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    /* =========================================================
       GET TEMPLATES
       ========================================================= */

    @Transactional
    public List<BeitragsstrukturDTO> getTemplates() {

        List<Beitragsstruktur> templates =
                repository.findByTemplateTrue();

        if (templates.isEmpty()) {

            createDefaultTemplates();

            templates = repository.findByTemplateTrue();
        }

        return templates.stream()
                .map(mapper::toDTO)
                .toList();
    }

    /* =========================================================
       CREATE FROM TEMPLATE
       ========================================================= */

    @Transactional
    public BeitragsstrukturDTO createFromDefaultTemplate(
            String name
    ) {

        List<Beitragsstruktur> templates =
                repository.findByTemplateTrue();

        if (templates.isEmpty()) {

            createDefaultTemplates();

            templates = repository.findByTemplateTrue();
        }

        Beitragsstruktur template =
                templates.stream()
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Kein Standardtemplate vorhanden"
                                )
                        );

        Beitragsstruktur copy =
                copyFromTemplateEntity(
                        template.getId(),
                        name
                );

        return mapper.toDTO(copy);
    }

    /* =========================================================
       DEFAULT TEMPLATES
       ========================================================= */

    @Transactional
    protected void createDefaultTemplates() {

        Beitragsstruktur struktur = new Beitragsstruktur();

        struktur.setName("Standard Kinder/Jugend");
        struktur.setTemplate(true);
        struktur.setAktiv(true);
        struktur.setSystem(true);

        Beitragsregel r0 = new Beitragsregel();
        r0.setSortierung(0);
        r0.setAlterBis(2);
        r0.setBeitrag(BigDecimal.ZERO);
        r0.setStruktur(struktur);

        Beitragsregel r1 = new Beitragsregel();
        r1.setSortierung(1);
        r1.setAlterBis(10);
        r1.setBeitrag(BigDecimal.valueOf(100));
        r1.setStruktur(struktur);

        Beitragsregel r2 = new Beitragsregel();
        r2.setSortierung(2);
        r2.setAlterBis(20);
        r2.setBeitrag(BigDecimal.valueOf(200));
        r2.setStruktur(struktur);

        Beitragsregel r3 = new Beitragsregel();
        r3.setSortierung(3);
        r3.setAlterBis(26);
        r3.setBeitrag(BigDecimal.valueOf(300));
        r3.setStruktur(struktur);

        Beitragsregel r4 = new Beitragsregel();
        r4.setSortierung(4);
        r4.setAlterBis(null);
        r4.setBeitrag(BigDecimal.valueOf(400));
        r4.setStruktur(struktur);

        Beitragsregel m = new Beitragsregel();
        m.setSortierung(0);
        m.setAlterBis(null);
        m.setRolle(TeilnehmerRolle.MITARBEITER);
        m.setBeitrag(BigDecimal.ZERO);
        m.setStruktur(struktur);

        Beitragsregel l = new Beitragsregel();
        l.setSortierung(0);
        l.setAlterBis(null);
        l.setRolle(TeilnehmerRolle.LEITER);
        l.setBeitrag(BigDecimal.ZERO);
        l.setStruktur(struktur);

        struktur.setRegeln(
                List.of(r0, r1, r2, r3, r4, m, l)
        );

        validator.validate(struktur.getRegeln());

        repository.save(struktur);
    }

    /* =========================================================
       ADD REGEL
       ========================================================= */

    @Transactional
    public BeitragsstrukturDTO addRegel(
            Long strukturId,
            BeitragsregelCreateDTO dto
    ) {

        Beitragsstruktur struktur =
                repository.findById(strukturId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Struktur nicht gefunden"
                                )
                        );

        int nextSortierung =
                struktur.getRegeln()
                        .stream()
                        .filter(r ->
                                (r.getRolle() == null && dto.getRolle() == null)
                                        || (r.getRolle() == dto.getRolle())
                        )
                        .map(Beitragsregel::getSortierung)
                        .max(Integer::compareTo)
                        .orElse(-1) + 1;

        Beitragsregel regel = new Beitragsregel();

        regel.setStruktur(struktur);

        regel.setSortierung(nextSortierung);

        regel.setAlterBis(dto.getAlterBis());

        regel.setRolle(dto.getRolle());

        regel.setBeitrag(dto.getBeitrag());

        struktur.getRegeln().add(regel);

        validator.validate(struktur.getRegeln());

        Beitragsstruktur saved = repository.save(struktur);

        return mapper.toDTO(saved);
    }
}