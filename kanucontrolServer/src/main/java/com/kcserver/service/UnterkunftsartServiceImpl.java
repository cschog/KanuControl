package com.kcserver.service;

import com.kcserver.dto.unterkunft.UnterkunftsartCreateUpdateDTO;
import com.kcserver.dto.unterkunft.UnterkunftsartDTO;
import com.kcserver.dto.unterkunft.UnterkunftsartRefDTO;
import com.kcserver.entity.Unterkunftsart;
import com.kcserver.mapper.UnterkunftsartMapper;
import com.kcserver.repository.UnterkunftsartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.kcserver.exception.ErrorMessages.UNTERKUNFTSART_ALREADY_EXISTS;
import static com.kcserver.exception.ErrorMessages.UNTERKUNFTSART_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
public class UnterkunftsartServiceImpl implements UnterkunftsartService {

    private final UnterkunftsartRepository repository;
    private final UnterkunftsartMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<UnterkunftsartDTO> getAll() {
        return mapper.toDTO(repository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnterkunftsartDTO> getActive() {
        return mapper.toDTO(
                repository.findByAktivTrueOrderByBezeichnungAsc());
    }

    @Override
    @Transactional(readOnly = true)
    public UnterkunftsartDTO getById(Long id) {
        return mapper.toDTO(getEntity(id));
    }

    @Override
    public UnterkunftsartDTO create(
            UnterkunftsartCreateUpdateDTO dto) {
        if (repository.findByBezeichnung(dto.getBezeichnung()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    UNTERKUNFTSART_ALREADY_EXISTS);
        }
        Unterkunftsart entity = mapper.toEntity(dto);
        return mapper.toDTO(repository.save(entity));
    }

    @Override
    public UnterkunftsartDTO update(
            Long id,
            UnterkunftsartCreateUpdateDTO dto) {

        Unterkunftsart entity = getEntity(id);

        if (!entity.getBezeichnung().equals(dto.getBezeichnung())) {

            repository.findByBezeichnung(dto.getBezeichnung())
                    .ifPresent(u -> {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                UNTERKUNFTSART_ALREADY_EXISTS);
                    });
        }
        mapper.update(dto, entity);

        return mapper.toDTO(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnterkunftsartRefDTO> getRefs() {

        return mapper.toRef(
                repository.findByAktivTrueOrderByBezeichnungAsc());
    }

    @Override
    public void delete(Long id) {

        // TODO prüfen, ob Unterkunftsart bereits verwendet wird

        repository.delete(getEntity(id));
    }

    private Unterkunftsart getEntity(Long id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                UNTERKUNFTSART_NOT_FOUND));
    }
}