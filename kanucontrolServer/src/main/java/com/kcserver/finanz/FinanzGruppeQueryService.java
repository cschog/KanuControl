package com.kcserver.finanz;

import com.kcserver.dto.finanz.FinanzGruppeDetailDTO;
import com.kcserver.dto.finanz.FinanzGruppeOverviewDTO;
import com.kcserver.entity.FinanzGruppe;
import com.kcserver.mapper.FinanzGruppeDetailMapper;
import com.kcserver.mapper.FinanzGruppeOverviewMapper;
import com.kcserver.repository.AbrechnungBelegRepository;
import com.kcserver.repository.FinanzGruppeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FinanzGruppeQueryService {

    private final FinanzGruppeRepository repository;
    private final AbrechnungBelegRepository belegRepository;
    private final FinanzGruppeOverviewMapper overviewMapper;
    private final FinanzGruppeDetailMapper detailMapper;

    @Transactional(readOnly = true)
    public List<FinanzGruppeOverviewDTO> getOverview(Long veranstaltungId) {

        List<FinanzGruppe> gruppen =
                repository.findWithTeilnehmerByVeranstaltungId(veranstaltungId);

        // 🔹 1. Beleg-Counts gesammelt laden
        Map<Long, Long> belegCountMap =
                belegRepository.countByVeranstaltungGrouped(veranstaltungId)
                        .stream()
                        .collect(Collectors.toMap(
                                row -> (Long) row[0],
                                row -> (Long) row[1]
                        ));

        // 🔹 2. Mapping
        return gruppen.stream()
                .map(g -> {
                    long belegCount =
                            belegCountMap.getOrDefault(g.getId(), 0L);

                    return overviewMapper.toDTO(g, belegCount);
                })
                .toList();
    }
    @Transactional(readOnly = true)
    public FinanzGruppeDetailDTO getDetail(Long gruppeId) {

        FinanzGruppe gruppe = repository.findWithTeilnehmer(gruppeId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Finanzgruppe nicht gefunden"));

        return detailMapper.toDetailDTO(gruppe);
    }
}
