package com.kcserver.finanz;

import com.kcserver.dto.finanz.FinanzGruppeDetailDTO;
import com.kcserver.dto.finanz.FinanzGruppeOverviewDTO;
import com.kcserver.entity.FinanzGruppe;
import com.kcserver.mapper.FinanzGruppeDetailMapper;
import com.kcserver.repository.FinanzGruppeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FinanzGruppeQueryService {

    private final FinanzGruppeRepository repository;
    private final FinanzGruppeDetailMapper mapper;

    /* ==============================
       OVERVIEW (KürzelPage)
       ============================== */

    @Transactional(readOnly = true)
    public List<FinanzGruppeOverviewDTO> getOverview(Long veranstaltungId) {
        return repository.findOverviewByVeranstaltungId(veranstaltungId);
    }

    /* ==============================
       DETAIL
       ============================== */

    @Transactional(readOnly = true)
    public FinanzGruppeDetailDTO getDetail(Long gruppeId) {

        FinanzGruppe gruppe = repository.findWithTeilnehmer(gruppeId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Finanzgruppe nicht gefunden"));

        return mapper.toDetailDTO(gruppe);
    }
}
