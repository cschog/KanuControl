package com.kcserver.service.reisekosten;

import com.kcserver.dto.reisekosten.ReisekostenKonfigurationResponse;
import com.kcserver.dto.reisekosten.ReisekostenKonfigurationSaveRequest;
import com.kcserver.entity.ReisekostenKonfiguration;
import com.kcserver.repository.ReisekostenKonfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReisekostenKonfigurationServiceImpl
        implements ReisekostenKonfigurationService {

    private final ReisekostenKonfigurationRepository repository;

    @Override
    @Transactional(readOnly = true)
    public ReisekostenKonfigurationResponse getAktuell() {

        ReisekostenKonfiguration entity =
                repository.findFirstByGueltigBisIsNull()
                        .orElseThrow();

        return toResponse(entity);
    }

    @Override
    public Long create(
            ReisekostenKonfigurationSaveRequest request
    ) {

        repository.findFirstByGueltigBisIsNull()
                .ifPresent(current -> {

                    current.setGueltigBis(
                            request.gueltigVon().minusDays(1)
                    );
                });

        ReisekostenKonfiguration entity =
                new ReisekostenKonfiguration();

        entity.setPkwSatz(
                request.pkwSatz()
        );

        entity.setMitfahrerSatz(
                request.mitfahrerSatz()
        );

        entity.setAnhaengerSatz(
                request.anhaengerSatz()
        );

        entity.setGueltigVon(
                request.gueltigVon()
        );

        entity.setGueltigBis(null);

        repository.findFirstByGueltigBisIsNull()
                .ifPresent(current -> {
                    if (!request.gueltigVon()
                            .isAfter(current.getGueltigVon())) {
                        throw new IllegalArgumentException(
                                "gueltigVon must be after current configuration"
                        );
                    }
                });

        repository.save(entity);

        return entity.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReisekostenKonfigurationResponse> list() {

        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ReisekostenKonfigurationResponse toResponse(
            ReisekostenKonfiguration entity
    ) {

        return new ReisekostenKonfigurationResponse(
                entity.getId(),
                entity.getPkwSatz(),
                entity.getMitfahrerSatz(),
                entity.getAnhaengerSatz(),
                entity.getGueltigVon(),
                entity.getGueltigBis()
        );
    }
}