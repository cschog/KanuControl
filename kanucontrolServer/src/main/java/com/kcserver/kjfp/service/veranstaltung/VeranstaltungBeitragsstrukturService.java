package com.kcserver.kjfp.service.veranstaltung;

import com.kcserver.core.entity.Veranstaltung;
import com.kcserver.core.repository.VeranstaltungRepository;
import com.kcserver.core.exception.ErrorMessages;
import com.kcserver.kjfp.dto.beitrag.BeitragsstrukturDTO;
import com.kcserver.kjfp.entity.beitraege.Beitragsstruktur;
import com.kcserver.kjfp.mapper.BeitragsstrukturMapper;
import com.kcserver.kjfp.service.beitrag.BeitragsstrukturService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class VeranstaltungBeitragsstrukturService {
    private final VeranstaltungRepository veranstaltungRepository;
    private final BeitragsstrukturService beitragsstrukturService;
    private final BeitragsstrukturMapper beitragsstrukturMapper;

    @Transactional
    public BeitragsstrukturDTO assignBeitragsstrukturFromTemplate(
            Long veranstaltungId,
            Long templateId
    ) {

        Veranstaltung veranstaltung =
                veranstaltungRepository.findById(veranstaltungId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (veranstaltung.getBeitragsstruktur() != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    ErrorMessages.VERANSTALTUNG_ALREADY_HAS_BEITRAGSSTRUKTUR
            );
        }

        Beitragsstruktur struktur =
                beitragsstrukturService.copyFromTemplateEntity(
                        templateId,
                        "Beitragsstruktur " + veranstaltung.getName()
                );
        veranstaltung.setBeitragsstruktur(struktur);

        veranstaltungRepository.save(veranstaltung);

        return beitragsstrukturMapper.toDTO(struktur);

    }


}
