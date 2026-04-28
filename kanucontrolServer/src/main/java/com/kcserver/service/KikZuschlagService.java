package com.kcserver.service;

import com.kcserver.dto.kik.KikZuschlagCreateUpdateDTO;
import com.kcserver.dto.kik.KikZuschlagDTO;
import com.kcserver.entity.KikZuschlag;
import com.kcserver.repository.KikZuschlagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import com.kcserver.mapper.KikZuschlagMapper;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KikZuschlagService {

    private final KikZuschlagRepository repository;
    private final KikZuschlagMapper mapper;

    public KikZuschlag findOptionalGueltigAm(LocalDate datum) {
        return repository.findGueltigAm(datum).orElse(null);
    }

    public KikZuschlag findRequiredGueltigAm(LocalDate datum) {
        return repository.findGueltigAm(datum)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Kein gültiger KiK-Zuschlag gefunden"
                        ));
    }
    @Transactional
    public KikZuschlagDTO create(KikZuschlagCreateUpdateDTO dto) {

        KikZuschlag k = new KikZuschlag();

        k.setGueltigVon(dto.getGueltigVon());
        k.setGueltigBis(dto.getGueltigBis());
        k.setKikZuschlag(dto.getKikZuschlag());
        k.setBeschluss(dto.getBeschluss());

        return mapper.toDTO(repository.save(k));
    }

    public List<KikZuschlagDTO> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Transactional
    public KikZuschlagDTO update(Long id, KikZuschlagCreateUpdateDTO dto) {

        KikZuschlag k = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        k.setGueltigVon(dto.getGueltigVon());
        k.setGueltigBis(dto.getGueltigBis());
        k.setKikZuschlag(dto.getKikZuschlag());
        k.setBeschluss(dto.getBeschluss());

        return mapper.toDTO(repository.save(k));
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}