package com.kcserver.vermietung.service;

import com.kcserver.core.exception.ErrorMessages;
import com.kcserver.vermietung.dto.MietbereichRabattDTO;
import com.kcserver.vermietung.dto.MietbereichRabattSaveDTO;
import com.kcserver.vermietung.entity.Mietbereich;
import com.kcserver.vermietung.entity.MietbereichRabatt;
import com.kcserver.vermietung.enumtype.Rabattart;
import com.kcserver.vermietung.enumtype.RabattwertTyp;
import com.kcserver.vermietung.repository.MietbereichRabattRepository;
import com.kcserver.vermietung.repository.MietbereichRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class MietbereichRabattService {

    private final MietbereichRabattRepository mietbereichRabattRepository;
    private final MietbereichRepository mietbereichRepository;

    public MietbereichRabattService(
            MietbereichRabattRepository mietbereichRabattRepository,
            MietbereichRepository mietbereichRepository
    ) {
        this.mietbereichRabattRepository = mietbereichRabattRepository;
        this.mietbereichRepository = mietbereichRepository;
    }

    @Transactional(readOnly = true)
    public List<MietbereichRabattDTO> getAll(
            Long mietobjektId,
            Long mietbereichId
    ) {
        pruefeMietbereich(mietobjektId, mietbereichId);

        return mietbereichRabattRepository
                .findByMietbereichIdOrderByIdAsc(mietbereichId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public MietbereichRabattDTO getById(
            Long mietobjektId,
            Long mietbereichId,
            Long id
    ) {
        pruefeMietbereich(mietobjektId, mietbereichId);

        MietbereichRabatt rabatt = findeRabatt(mietbereichId, id);

        return toDTO(rabatt);
    }

    public MietbereichRabattDTO create(
            Long mietobjektId,
            Long mietbereichId,
            MietbereichRabattSaveDTO dto
    ) {
        Mietbereich mietbereich =
                pruefeMietbereich(mietobjektId, mietbereichId);

        validiere(dto);

        if (mietbereichRabattRepository.existsByMietbereichIdAndRabattart(
                mietbereichId,
                dto.getRabattart()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    ErrorMessages.MIETBEREICH_RABATT_DUPLICATE
            );
        }

        MietbereichRabatt rabatt = new MietbereichRabatt();
        rabatt.setMietbereich(mietbereich);

        uebernehmeDaten(rabatt, dto);

        return toDTO(mietbereichRabattRepository.save(rabatt));
    }

    public MietbereichRabattDTO update(
            Long mietobjektId,
            Long mietbereichId,
            Long id,
            MietbereichRabattSaveDTO dto
    ) {
        pruefeMietbereich(mietobjektId, mietbereichId);

        MietbereichRabatt rabatt = findeRabatt(mietbereichId, id);

        validiere(dto);

        if (mietbereichRabattRepository
                .existsByMietbereichIdAndRabattartAndIdNot(
                        mietbereichId,
                        dto.getRabattart(),
                        id
                )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    ErrorMessages.MIETBEREICH_RABATT_DUPLICATE
            );
        }

        uebernehmeDaten(rabatt, dto);

        return toDTO(mietbereichRabattRepository.save(rabatt));
    }

    public void delete(
            Long mietobjektId,
            Long mietbereichId,
            Long id
    ) {
        pruefeMietbereich(mietobjektId, mietbereichId);

        MietbereichRabatt rabatt = findeRabatt(mietbereichId, id);

        mietbereichRabattRepository.delete(rabatt);
    }

    private Mietbereich pruefeMietbereich(
            Long mietobjektId,
            Long mietbereichId
    ) {
        Mietbereich mietbereich = mietbereichRepository
                .findById(mietbereichId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        ErrorMessages.MIETBEREICH_NOT_FOUND
                ));

        if (!mietbereich.getMietobjekt().getId().equals(mietobjektId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.MIETBEREICH_RABATT_WRONG_MIETBEREICH
            );
        }

        return mietbereich;
    }

    private MietbereichRabatt findeRabatt(
            Long mietbereichId,
            Long id
    ) {
        return mietbereichRabattRepository
                .findByIdAndMietbereichId(id, mietbereichId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        ErrorMessages.MIETBEREICH_RABATT_NOT_FOUND
                ));
    }

    private void validiere(MietbereichRabattSaveDTO dto) {

        if (dto.getRabattart() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.MIETBEREICH_RABATT_ART_REQUIRED
            );
        }

        if (dto.getRabattart() == Rabattart.INDIVIDUELL) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.MIETBEREICH_RABATT_INDIVIDUELL_UNZULAESSIG
            );
        }

        if (dto.getRabattwertTyp() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.MIETBEREICH_RABATT_WERT_TYP_REQUIRED
            );
        }

        if (dto.getRabattwert() == null
                || dto.getRabattwert().signum() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.MIETBEREICH_RABATT_WERT_INVALID
            );
        }

        if (dto.getRabattwertTyp() == RabattwertTyp.PROZENTUAL
                && dto.getRabattwert().compareTo(new BigDecimal("100")) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.MIETBEREICH_RABATT_PROZENT_INVALID
            );
        }
    }

    private void uebernehmeDaten(
            MietbereichRabatt rabatt,
            MietbereichRabattSaveDTO dto
    ) {
        rabatt.setRabattart(dto.getRabattart());
        rabatt.setRabattwertTyp(dto.getRabattwertTyp());
        rabatt.setRabattwert(dto.getRabattwert());
        rabatt.setAktiv(dto.isAktiv());
    }

    private MietbereichRabattDTO toDTO(
            MietbereichRabatt rabatt
    ) {
        return new MietbereichRabattDTO(
                rabatt.getId(),
                rabatt.getMietbereich().getId(),
                rabatt.getRabattart(),
                rabatt.getRabattwertTyp(),
                rabatt.getRabattwert(),
                rabatt.isAktiv()
        );
    }
}