package com.kcserver.service.vermietung;

import com.kcserver.dto.vermietung.MietbereichDTO;
import com.kcserver.entity.vermietung.Mietbereich;
import com.kcserver.entity.vermietung.Mietobjekt;
import com.kcserver.repository.vermietung.MietbereichRepository;
import com.kcserver.repository.vermietung.MietobjektRepository;
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

        return mietbereichRepository.findByMietobjektId(mietobjektId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public MietbereichDTO getById(Long id) {

        Mietbereich bereich = mietbereichRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Mietbereich nicht gefunden: " + id
                        )
                );

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
            Long id,
            MietbereichDTO dto
    ) {

        Mietbereich bereich = mietbereichRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Mietbereich nicht gefunden: " + id
                        )
                );

        Mietobjekt mietobjekt = mietobjektRepository
                .findById(dto.getMietobjektId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Mietobjekt nicht gefunden: "
                                        + dto.getMietobjektId()
                        )
                );

        bereich.setMietobjekt(mietobjekt);
        bereich.setBezeichnung(dto.getBezeichnung());
        bereich.setBeschreibung(dto.getBeschreibung());
        bereich.setMietbar(dto.isMietbar());

        return toDTO(mietbereichRepository.save(bereich));
    }

    public void delete(Long id) {

        if (!mietbereichRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Mietbereich nicht gefunden: " + id
            );
        }

        mietbereichRepository.deleteById(id);
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