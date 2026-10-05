package com.kcserver.kjfp.service;

import com.kcserver.core.exception.ErrorMessages;
import com.kcserver.kjfp.dto.verpflegung.VerpflegungsmodellCreateUpdateDTO;
import com.kcserver.kjfp.dto.verpflegung.VerpflegungsmodellDTO;
import com.kcserver.kjfp.dto.verpflegung.VerpflegungsmodellRefDTO;
import com.kcserver.kjfp.entity.Verpflegungsmodell;
import com.kcserver.kjfp.mapper.VerpflegungsmodellMapper;
import com.kcserver.kjfp.repository.VerpflegungsmodellRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VerpflegungsmodellServiceImpl implements VerpflegungsmodellService {

    private final VerpflegungsmodellRepository repository;
    private final VerpflegungsmodellMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<VerpflegungsmodellDTO> getAll() {
        return mapper.toDTO(repository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VerpflegungsmodellDTO> getActive() {
        return mapper.toDTO(
                repository.findByAktivTrueOrderByBezeichnungAsc());
    }

    @Override
    @Transactional(readOnly = true)
    public VerpflegungsmodellDTO getById(Long id) {
        return mapper.toDTO(getEntity(id));
    }

    @Override
    public VerpflegungsmodellDTO create(
            VerpflegungsmodellCreateUpdateDTO dto) {
        if (repository.findByBezeichnung(dto.getBezeichnung()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    ErrorMessages.VERPFLEGUNGSMODELL_ALREADY_EXISTS);
        }
        Verpflegungsmodell entity = mapper.toEntity(dto);
        return mapper.toDTO(repository.save(entity));
    }

    @Override
    public VerpflegungsmodellDTO update(
            Long id,
            VerpflegungsmodellCreateUpdateDTO dto) {

        Verpflegungsmodell entity = getEntity(id);

        if (!entity.getBezeichnung().equals(dto.getBezeichnung())) {

            repository.findByBezeichnung(dto.getBezeichnung())
                    .ifPresent(u -> {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                ErrorMessages.VERPFLEGUNGSMODELL_ALREADY_EXISTS);
                    });
        }
        mapper.update(dto, entity);

        return mapper.toDTO(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VerpflegungsmodellRefDTO> getRefs() {

        return mapper.toRef(
                repository.findByAktivTrueOrderByBezeichnungAsc());
    }

    @Override
    public void delete(Long id) {

        // TODO prüfen, ob Verpflegungsmodell bereits verwendet wird

        repository.delete(getEntity(id));
    }

    private Verpflegungsmodell getEntity(Long id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                ErrorMessages.VERPFLEGUNGSMODELL_NOT_FOUND));
    }
}