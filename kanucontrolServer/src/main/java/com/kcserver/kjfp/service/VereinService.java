package com.kcserver.kjfp.service;

import com.kcserver.kjfp.dto.verein.VereinDTO;
import com.kcserver.kjfp.dto.verein.VereinRefDTO;
import com.kcserver.kjfp.entity.Person;
import com.kcserver.kjfp.entity.Verein;
import com.kcserver.core.exception.ErrorMessages;
import com.kcserver.kjfp.mapper.VereinMapper;
import com.kcserver.kjfp.repository.MitgliedRepository;
import com.kcserver.kjfp.repository.PersonRepository;
import com.kcserver.kjfp.repository.VeranstaltungRepository;
import com.kcserver.kjfp.repository.VereinRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class VereinService {

    private final VereinRepository vereinRepository;
    private final PersonRepository personRepository;
    private final VereinMapper vereinMapper;
    private final MitgliedRepository mitgliedRepository;
    private final VeranstaltungRepository veranstaltungRepository;
    private final VereinDataStatusService vereinDataStatusService;
    private final BankLookupService bankLookupService;

    public VereinService(
            VereinRepository vereinRepository,
            PersonRepository personRepository,
            VereinMapper vereinMapper,
            MitgliedRepository mitgliedRepository,
            VeranstaltungRepository veranstaltungRepository,
            VereinDataStatusService vereinDataStatusService,
            BankLookupService bankLookupService

    ) {
        this.vereinRepository = vereinRepository;
        this.personRepository = personRepository;
        this.vereinMapper = vereinMapper;
        this.veranstaltungRepository = veranstaltungRepository;
        this.mitgliedRepository = mitgliedRepository;
        this.vereinDataStatusService = vereinDataStatusService;
        this.bankLookupService = bankLookupService;
    }

    /* =========================================================
       READ
       ========================================================= */

    @Transactional(readOnly = true)
    public List<VereinDTO> getAll() {

        List<Verein> vereine = vereinRepository.findAll();

        if (vereine.isEmpty()) {
            return List.of();
        }

        Set<Long> vereinIds = vereine.stream()
                .map(Verein::getId)
                .collect(Collectors.toSet());

        Set<Long> veranstalterIds =
                veranstaltungRepository.findVeranstalterVereinIds(vereinIds);

        return vereine.stream()
                .map(verein -> {
                    VereinDTO dto = vereinMapper.toDTO(verein);

                    boolean isVeranstalter =
                            veranstalterIds.contains(verein.getId());

                    dto.setDataStatus(
                            vereinDataStatusService.determineStatus(
                                    verein,
                                    isVeranstalter
                            )
                    );

                    return dto;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public VereinDTO getById(Long id) {
        Verein verein = vereinRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, ErrorMessages.VEREIN_NOT_FOUND
                ));
        VereinDTO dto = vereinMapper.toDTO(verein);

        boolean isVeranstalter =
                veranstaltungRepository.existsByVereinId(verein.getId());

        dto.setDataStatus(
                vereinDataStatusService.determineStatus(
                        verein,
                        isVeranstalter
                )
        );

        return dto;
    }

    @Transactional(readOnly = true)
    public Page<VereinDTO> search(String name, String abk, Pageable pageable) {

        Specification<Verein> spec = buildSpec(name, abk);

        return vereinRepository.findAll(spec, pageable)
                .map(vereinMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<VereinDTO> searchAll(String name, String abk) {

        return vereinRepository.findAll(buildSpec(name, abk))
                .stream()
                .map(vereinMapper::toDTO)
                .toList();
    }

    private Specification<Verein> buildSpec(String name, String abk) {

        Specification<Verein> spec = (root, query, cb) -> cb.conjunction();

        if (name != null && !name.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%")
            );
        }

        if (abk != null && !abk.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("abk")), abk.toLowerCase())
            );
        }

        return spec;
    }


    public List<VereinRefDTO> searchRefList(String search) {
        return vereinRepository.searchRefList(search)
                .stream()
                .map(vereinMapper::toRefDTO)
                .toList();
    }

    /* =========================================================
       CREATE
       ========================================================= */

    public VereinDTO create(VereinDTO dto) {

        normalize(dto);

        // 🔒 Fachliche Duplikatsprüfung
        ensureUniqueVerein(dto.getAbk(), dto.getName(), null);

        Verein verein = vereinMapper.toEntity(dto);

        if (dto.getKontoinhaberId() != null) {
            Person kontoinhaber = personRepository.findById(dto.getKontoinhaberId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, ErrorMessages.KONTOINHABER_NOT_FOUND
                    ));
            verein.setKontoinhaber(kontoinhaber);
        }

        Verein saved = vereinRepository.save(verein);

        VereinDTO result = vereinMapper.toDTO(saved);

        result.setDataStatus(
                vereinDataStatusService.determineStatus(
                        saved,
                        false
                )
        );

        return result;
    }

    private void ensureUniqueVerein(String abk, String name, Long excludeId) {
        vereinRepository.findByAbkAndName(abk, name)
                .ifPresent(v -> {
                    if (!v.getId().equals(excludeId)) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                ErrorMessages.VEREIN_ALREADY_EXISTS
                        );
                    }
                });
    }

    /* =========================================================
       UPDATE
       ========================================================= */

    public VereinDTO update(Long id, VereinDTO dto) {

        normalize(dto);

        Verein verein = vereinRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, ErrorMessages.VEREIN_NOT_FOUND
                ));

        // 🔒 Fachliche Duplikatsprüfung (UPDATE!)
        ensureUniqueVerein(dto.getAbk(), dto.getName(), id);

        vereinMapper.updateFromDTO(dto, verein);

        bankLookupService.fillBankNameIfMissing(verein);

        // Kontoinhaber entfernen
        if (dto.getKontoinhaberId() == null) {
            verein.setKontoinhaber(null);
        }
        // Kontoinhaber setzen oder wechseln
        else if (
                verein.getKontoinhaber() == null ||
                        !verein.getKontoinhaber().getId().equals(dto.getKontoinhaberId())
        ) {
            Person neuerInhaber = personRepository.findById(dto.getKontoinhaberId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, ErrorMessages.KONTOINHABER_NOT_FOUND
                    ));
            verein.setKontoinhaber(neuerInhaber);
        }

        VereinDTO result = vereinMapper.toDTO(verein);

        boolean isVeranstalter =
                veranstaltungRepository.existsByVereinId(verein.getId());

        result.setDataStatus(
                vereinDataStatusService.determineStatus(
                        verein,
                        isVeranstalter
                )
        );

        return result;
    }

    /* =========================================================
       DELETE
       ========================================================= */

    @Transactional

    public void delete(Long id) {

        Verein verein = getVereinOrThrow(id);

        if (mitgliedRepository.existsByVereinId(id)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                   ErrorMessages.VEREIN_KANN_NICHT_GELOESCHT_WERDEN_MITGLIEDER
            );
        }

        if (veranstaltungRepository.existsByVereinId(id)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    ErrorMessages.VEREIN_KANN_NICHT_GELOESCHT_WERDEN_VERANSTALTER
            );
        }

        vereinRepository.delete(verein);

    }

    private Verein getVereinOrThrow(Long id) {
        return vereinRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        ErrorMessages.VEREIN_NOT_FOUND
                ));
    }

    private void normalize(VereinDTO dto) {
        if (dto.getName() != null) {
            dto.setName(dto.getName().trim());
        }
        if (dto.getAbk() != null) {
            dto.setAbk(dto.getAbk().trim());
        }
    }
}