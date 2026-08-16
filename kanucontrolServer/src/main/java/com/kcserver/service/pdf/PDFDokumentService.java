package com.kcserver.service.pdf;

import com.kcserver.dto.teilnehmer.TeilnehmerDetailDTO;
import com.kcserver.dto.veranstaltung.VeranstaltungDetailDTO;
import com.kcserver.enumtype.PdfDokumentTyp;
import com.kcserver.service.TeilnehmerService;
import com.kcserver.service.veranstaltung.VeranstaltungService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PDFDokumentService {

    private final DokumentValidationService dokumentValidationService;

    private final PDFAnmeldungFmJemService fmJemReportService;
    private final PDFAbrechnungService abrechnungService;
    private final PDFErhebungsbogenService erhebungsbogenService;
    private final PDFTeilnehmerlisteService teilnehmerlisteService;
    private final PDFZahlungsnachweiseService zahlungsnachweiseService;
    private final PDFBelegDokumenteService belegDokumenteService;

    private final VeranstaltungService veranstaltungService;
    private final TeilnehmerService teilnehmerService;

    public byte[] generateZahlungsnachweise(
            Long veranstaltungId
    ) {

        return zahlungsnachweiseService.generate(
                veranstaltungId
        );
    }

    public byte[] generateBelege(
            Long veranstaltungId
    ) {

        return belegDokumenteService.generate(
                veranstaltungId
        );
    }

    public byte[] generateAnmeldung(
            Long veranstaltungId
    ) {

        validate(
                veranstaltungId,
                PdfDokumentTyp.ANMELDUNG
        );

        return fmJemReportService.generate(
                veranstaltungId
        );
    }

    public byte[] generateAbrechnung(
            Long veranstaltungId
    ) {

        validate(
                veranstaltungId,
                PdfDokumentTyp.ABRECHNUNG
        );

        return abrechnungService.generate(
                veranstaltungId
        );
    }

    public byte[] generateErhebungsbogen(
            Long veranstaltungId
    ) {

        validate(
                veranstaltungId,
                PdfDokumentTyp.ERHEBUNGSBOGEN
        );

        return erhebungsbogenService.generate(
                veranstaltungId
        );
    }

    public byte[] generateTeilnehmerliste(
            Long veranstaltungId
    ) {

        validate(
                veranstaltungId,
                PdfDokumentTyp.TEILNEHMERLISTE
        );

        VeranstaltungDetailDTO veranstaltung =
                veranstaltungService.getById(
                        veranstaltungId
                );

        List<TeilnehmerDetailDTO> teilnehmer =
                teilnehmerService.findAllDetails(
                        veranstaltungId
                );

        return teilnehmerlisteService.generate(
                veranstaltung,
                teilnehmer
        );
    }

    private void validate(
            Long veranstaltungId,
            PdfDokumentTyp dokumentTyp
    ) {

        ValidationResult result =
                dokumentValidationService.validate(
                        veranstaltungId,
                        dokumentTyp
                );

        if (!result.isValid()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.join(
                            System.lineSeparator(),
                            result.getMessages()
                    )
            );
        }
    }
}