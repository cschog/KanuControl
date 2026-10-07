package com.kcserver.vermietung.service;

import com.kcserver.vermietung.dto.MietbereichEigenschaftDTO;
import com.kcserver.vermietung.entity.Mietbereich;
import com.kcserver.vermietung.entity.MietbereichEigenschaft;
import com.kcserver.vermietung.repository.MietbereichEigenschaftRepository;
import com.kcserver.vermietung.repository.MietbereichRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MietbereichEigenschaftService {

    private final MietbereichEigenschaftRepository
            mietbereichEigenschaftRepository;

    private final MietbereichRepository mietbereichRepository;

    public MietbereichEigenschaftService(
            MietbereichEigenschaftRepository mietbereichEigenschaftRepository,
            MietbereichRepository mietbereichRepository
    ) {
        this.mietbereichEigenschaftRepository =
                mietbereichEigenschaftRepository;
        this.mietbereichRepository = mietbereichRepository;
    }

    @Transactional(readOnly = true)
    public List<MietbereichEigenschaftDTO> getAll(
            Long mietobjektId,
            Long mietbereichId
    ) {

        Mietbereich bereich = getMietbereich(
                mietobjektId,
                mietbereichId
        );

        return mietbereichEigenschaftRepository
                .findByMietbereichIdOrderBySortierungAscIdAsc(bereich.getId())
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public MietbereichEigenschaftDTO getById(
            Long mietobjektId,
            Long mietbereichId,
            Long id
    ) {

        Mietbereich bereich = getMietbereich(
                mietobjektId,
                mietbereichId
        );

        MietbereichEigenschaft eigenschaft =
                mietbereichEigenschaftRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Mietbereich-Eigenschaft nicht gefunden: "
                                                + id
                                )
                        );

        if (!eigenschaft.getMietbereich().getId()
                .equals(bereich.getId())) {

            throw new IllegalArgumentException(
                    "Mietbereich-Eigenschaft gehört nicht zum angegebenen Mietbereich."
            );
        }

        return toDTO(eigenschaft);
    }

    public MietbereichEigenschaftDTO create(
            Long mietobjektId,
            Long mietbereichId,
            MietbereichEigenschaftDTO dto
    ) {

        Mietbereich bereich = getMietbereich(
                mietobjektId,
                mietbereichId
        );

        MietbereichEigenschaft eigenschaft =
                new MietbereichEigenschaft();

        eigenschaft.setMietbereich(bereich);
        eigenschaft.setSortierung(dto.getSortierung());
        eigenschaft.setBezeichnung(dto.getBezeichnung());
        eigenschaft.setTyp(dto.getTyp());
        eigenschaft.setWert(dto.getWert());

        return toDTO(
                mietbereichEigenschaftRepository.save(eigenschaft)
        );
    }

    public MietbereichEigenschaftDTO update(
            Long mietobjektId,
            Long mietbereichId,
            Long id,
            MietbereichEigenschaftDTO dto
    ) {

        Mietbereich bereich = getMietbereich(
                mietobjektId,
                mietbereichId
        );

        MietbereichEigenschaft eigenschaft =
                mietbereichEigenschaftRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Mietbereich-Eigenschaft nicht gefunden: "
                                                + id
                                )
                        );

        if (!eigenschaft.getMietbereich().getId()
                .equals(bereich.getId())) {

            throw new IllegalArgumentException(
                    "Mietbereich-Eigenschaft gehört nicht zum angegebenen Mietbereich."
            );
        }

        eigenschaft.setSortierung(dto.getSortierung());
        eigenschaft.setBezeichnung(dto.getBezeichnung());
        eigenschaft.setTyp(dto.getTyp());
        eigenschaft.setWert(dto.getWert());

        return toDTO(
                mietbereichEigenschaftRepository.save(eigenschaft)
        );
    }

    public void delete(
            Long mietobjektId,
            Long mietbereichId,
            Long id
    ) {

        Mietbereich bereich = getMietbereich(
                mietobjektId,
                mietbereichId
        );

        MietbereichEigenschaft eigenschaft =
                mietbereichEigenschaftRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Mietbereich-Eigenschaft nicht gefunden: "
                                                + id
                                )
                        );

        if (!eigenschaft.getMietbereich().getId()
                .equals(bereich.getId())) {

            throw new IllegalArgumentException(
                    "Mietbereich-Eigenschaft gehört nicht zum angegebenen Mietbereich."
            );
        }

        mietbereichEigenschaftRepository.delete(eigenschaft);
    }

    private Mietbereich getMietbereich(
            Long mietobjektId,
            Long mietbereichId
    ) {

        Mietbereich bereich = mietbereichRepository.findById(mietbereichId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Mietbereich nicht gefunden: "
                                        + mietbereichId
                        )
                );

        if (!bereich.getMietobjekt().getId().equals(mietobjektId)) {
            throw new IllegalArgumentException(
                    "Mietbereich gehört nicht zum angegebenen Mietobjekt."
            );
        }

        return bereich;
    }

    private MietbereichEigenschaftDTO toDTO(
            MietbereichEigenschaft eigenschaft
    ) {

        MietbereichEigenschaftDTO dto =
                new MietbereichEigenschaftDTO();

        dto.setId(eigenschaft.getId());
        dto.setMietbereichId(
                eigenschaft.getMietbereich().getId()
        );
        dto.setSortierung(eigenschaft.getSortierung());
        dto.setBezeichnung(eigenschaft.getBezeichnung());
        dto.setTyp(eigenschaft.getTyp());
        dto.setWert(eigenschaft.getWert());

        return dto;
    }
}