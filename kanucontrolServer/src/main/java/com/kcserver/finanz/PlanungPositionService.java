package com.kcserver.finanz;

import com.kcserver.dto.planung.PlanungPositionCreateDTO;
import com.kcserver.dto.planung.PlanungPositionDTO;
import com.kcserver.entity.Planung;
import com.kcserver.entity.PlanungPosition;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.mapper.PlanungMapper;
import com.kcserver.repository.PlanungPositionRepository;
import com.kcserver.repository.PlanungRepository;
import com.kcserver.repository.VeranstaltungRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class PlanungPositionService {

    private final PlanungRepository planungRepository;
    private final PlanungPositionRepository positionRepository;
    private final VeranstaltungRepository veranstaltungRepository;
    private final PlanungMapper mapper;

    /* =========================================================
       ADD
       ========================================================= */

    public PlanungPositionDTO addPosition(Long veranstaltungId,
                                          PlanungPositionCreateDTO dto) {

        Planung planung = getPlanung(veranstaltungId);

        assertEditable(planung);

        PlanungPosition position = new PlanungPosition();
        position.setKategorie(dto.getKategorie());
        position.setBetrag(dto.getBetrag());
        position.setKommentar(dto.getKommentar());

        planung.addPosition(position);

        positionRepository.save(position);

        return mapper.toPositionDTO(position);
    }

    /* =========================================================
       UPDATE
       ========================================================= */

    public PlanungPositionDTO updatePosition(Long veranstaltungId,
                                             Long positionId,
                                             PlanungPositionCreateDTO dto) {

        PlanungPosition pos = positionRepository
                .findById(positionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Position nicht gefunden"
                ));

        validateVeranstaltung(veranstaltungId, pos);

        assertEditable(pos.getPlanung());

        pos.setKategorie(dto.getKategorie());
        pos.setBetrag(dto.getBetrag());
        pos.setKommentar(dto.getKommentar());

        return mapper.toPositionDTO(pos);
    }

    /* =========================================================
       DELETE
       ========================================================= */

    public void deletePosition(Long veranstaltungId, Long positionId) {

        PlanungPosition pos = positionRepository
                .findById(positionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Position nicht gefunden"
                ));

        validateVeranstaltung(veranstaltungId, pos);

        Planung planung = pos.getPlanung();

        assertEditable(planung);

        planung.removePosition(pos);

        positionRepository.delete(pos);
    }

    /* ========================================================= */

    private Planung getPlanung(Long veranstaltungId) {

        return planungRepository
                .findByVeranstaltungId(veranstaltungId)
                .orElseGet(() -> {

                    Veranstaltung v = veranstaltungRepository
                            .findById(veranstaltungId)
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Veranstaltung nicht gefunden"
                            ));

                    Planung p = new Planung();
                    p.setVeranstaltung(v);

                    return planungRepository.save(p);
                });
    }

    private void validateVeranstaltung(Long veranstaltungId,
                                       PlanungPosition pos) {

        if (!pos.getPlanung()
                .getVeranstaltung()
                .getId()
                .equals(veranstaltungId)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Position gehört zu anderer Veranstaltung"
            );
        }
    }

    private void assertEditable(Planung planung) {

        if (planung.isEingereicht()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Planung bereits eingereicht"
            );
        }
    }
}