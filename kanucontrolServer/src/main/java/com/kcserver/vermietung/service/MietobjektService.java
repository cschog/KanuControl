package com.kcserver.vermietung.service;

import com.kcserver.vermietung.dto.MietobjektDTO;
import com.kcserver.kjfp.entity.Verein;
import com.kcserver.vermietung.entity.Mietobjekt;
import com.kcserver.kjfp.repository.VereinRepository;
import com.kcserver.vermietung.repository.MietobjektRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MietobjektService {

    private final MietobjektRepository mietobjektRepository;
    private final VereinRepository vereinRepository;

    public MietobjektService(
            MietobjektRepository mietobjektRepository,
            VereinRepository vereinRepository
    ) {
        this.mietobjektRepository = mietobjektRepository;
        this.vereinRepository = vereinRepository;
    }

    @Transactional(readOnly = true)
    public List<MietobjektDTO> getAll() {

        return mietobjektRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public MietobjektDTO getById(Long id) {

        Mietobjekt objekt = mietobjektRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Mietobjekt nicht gefunden: " + id
                        )
                );

        return toDTO(objekt);
    }

    public MietobjektDTO create(MietobjektDTO dto) {

        Verein verein = vereinRepository.findById(dto.getVereinId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Verein nicht gefunden: " + dto.getVereinId()
                        )
                );

        Mietobjekt objekt = new Mietobjekt();

        objekt.setVerein(verein);
        objekt.setBezeichnung(dto.getBezeichnung());
        objekt.setBeschreibung(dto.getBeschreibung());
        objekt.setStrasse(dto.getStrasse());
        objekt.setPlz(dto.getPlz());
        objekt.setOrt(dto.getOrt());
        objekt.setCountryCode(dto.getCountryCode());

        mietobjektRepository.unsetAktivesMietobjekt();
        mietobjektRepository.flush();

        objekt.setAktiv(true);
        objekt.setMietbar(dto.isMietbar());

        return toDTO(mietobjektRepository.save(objekt));
    }

    public MietobjektDTO update(
            Long id,
            MietobjektDTO dto
    ) {

        Mietobjekt objekt = mietobjektRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Mietobjekt nicht gefunden: " + id
                        )
                );

        Verein verein = vereinRepository.findById(dto.getVereinId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Verein nicht gefunden: " + dto.getVereinId()
                        )
                );

        objekt.setVerein(verein);
        objekt.setBezeichnung(dto.getBezeichnung());
        objekt.setBeschreibung(dto.getBeschreibung());
        objekt.setStrasse(dto.getStrasse());
        objekt.setPlz(dto.getPlz());
        objekt.setOrt(dto.getOrt());
        objekt.setCountryCode(dto.getCountryCode());

        // aktiv wird ausschließlich über setActive() geändert
        objekt.setMietbar(dto.isMietbar());

        return toDTO(mietobjektRepository.save(objekt));
    }
    public void delete(Long id) {

        Mietobjekt objekt = mietobjektRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Mietobjekt nicht gefunden: " + id
                        )
                );

        boolean warAktiv = objekt.isAktiv();

        mietobjektRepository.delete(objekt);
        mietobjektRepository.flush();

        if (warAktiv) {
            Optional<Mietobjekt> neuAktiv =
                    mietobjektRepository
                            .findTopByIdNotOrderByIdDesc(id);

            neuAktiv.ifPresent(mietobjekt -> {
                mietobjekt.setAktiv(true);
                mietobjektRepository.save(mietobjekt);
            });
        }
    }

    private MietobjektDTO toDTO(Mietobjekt objekt) {

        MietobjektDTO dto = new MietobjektDTO();

        dto.setId(objekt.getId());
        dto.setVereinId(objekt.getVerein().getId());
        dto.setBezeichnung(objekt.getBezeichnung());
        dto.setBeschreibung(objekt.getBeschreibung());
        dto.setStrasse(objekt.getStrasse());
        dto.setPlz(objekt.getPlz());
        dto.setOrt(objekt.getOrt());
        dto.setCountryCode(objekt.getCountryCode());
        dto.setAktiv(objekt.isAktiv());
        dto.setMietbar(objekt.isMietbar());

        return dto;
    }

    public MietobjektDTO setActive(Long mietobjektId) {

        Mietobjekt neu = mietobjektRepository.findById(mietobjektId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND
                ));

        mietobjektRepository.unsetAktivesMietobjekt();
        mietobjektRepository.flush();

        neu.setAktiv(true);
        mietobjektRepository.save(neu);

        return toDTO(neu);
    }

    @Transactional(readOnly = true)
    public MietobjektDTO getActive() {

        return mietobjektRepository.findByAktivTrue()
                .map(this::toDTO)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Kein aktives Mietobjekt vorhanden."
                ));
    }
}