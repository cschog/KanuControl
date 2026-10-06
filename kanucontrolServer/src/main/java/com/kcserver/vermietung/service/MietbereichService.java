package com.kcserver.vermietung.service;

import com.kcserver.vermietung.dto.MietbereichDTO;
import com.kcserver.vermietung.entity.Mietbereich;
import com.kcserver.vermietung.entity.Mietobjekt;
import com.kcserver.vermietung.repository.MietbereichRepository;
import com.kcserver.vermietung.repository.MietobjektRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MietbereichService {

    private final MietbereichRepository mietbereichRepository;
    private final MietobjektRepository mietobjektRepository;

    public MietbereichService(
            MietbereichRepository mietbereichRepository,
            MietobjektRepository mietobjektRepository
    ) {
        this.mietbereichRepository = mietbereichRepository;
        this.mietobjektRepository = mietobjektRepository;
    }

    @Transactional(readOnly = true)
    public List<MietbereichDTO> getAll(Long mietobjektId) {

        if (!mietobjektRepository.existsById(mietobjektId)) {
            throw new IllegalArgumentException(
                    "Mietobjekt nicht gefunden: " + mietobjektId
            );
        }

        return mietbereichRepository.findByMietobjektId(mietobjektId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public MietbereichDTO getById(Long mietobjektId, Long id) {

        Mietbereich bereich = mietbereichRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Mietbereich nicht gefunden: " + id
                        )
                );

        if (!bereich.getMietobjekt().getId().equals(mietobjektId)) {
            throw new IllegalArgumentException(
                    "Mietbereich gehört nicht zum angegebenen Mietobjekt."
            );
        }

        return toDTO(bereich);
    }

    public MietbereichDTO create(MietbereichDTO dto) {

        Mietobjekt mietobjekt = mietobjektRepository
                .findById(dto.getMietobjektId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Mietobjekt nicht gefunden: "
                                        + dto.getMietobjektId()
                        )
                );

        Mietbereich bereich = new Mietbereich();

        bereich.setMietobjekt(mietobjekt);
        bereich.setBezeichnung(dto.getBezeichnung());
        bereich.setBeschreibung(dto.getBeschreibung());
        bereich.setMietbar(dto.isMietbar());

        return toDTO(mietbereichRepository.save(bereich));
    }

    public MietbereichDTO update(
            Long mietobjektId,
            Long id,
            MietbereichDTO dto
    ) {
        Mietbereich bereich = mietbereichRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Mietbereich nicht gefunden: " + id
                        )
                );

        if (!bereich.getMietobjekt().getId().equals(mietobjektId)) {
            throw new IllegalArgumentException(
                    "Mietbereich gehört nicht zum angegebenen Mietobjekt."
            );
        }

        bereich.setBezeichnung(dto.getBezeichnung());
        bereich.setBeschreibung(dto.getBeschreibung());
        bereich.setMietbar(dto.isMietbar());

        return toDTO(mietbereichRepository.save(bereich));
    }

    public void delete(Long mietobjektId, Long id) {

        Mietbereich bereich = mietbereichRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Mietbereich nicht gefunden: " + id
                        )
                );

        if (!bereich.getMietobjekt().getId().equals(mietobjektId)) {
            throw new IllegalArgumentException(
                    "Mietbereich gehört nicht zum angegebenen Mietobjekt."
            );
        }

        mietbereichRepository.delete(bereich);
    }

    private MietbereichDTO toDTO(Mietbereich bereich) {

        MietbereichDTO dto = new MietbereichDTO();

        dto.setId(bereich.getId());
        dto.setMietobjektId(bereich.getMietobjekt().getId());
        dto.setBezeichnung(bereich.getBezeichnung());
        dto.setBeschreibung(bereich.getBeschreibung());
        dto.setMietbar(bereich.isMietbar());

        return dto;
    }
}