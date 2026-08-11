package com.kcserver.service.abrechnung;

import com.kcserver.dto.zahlungsnachweis.ZahlungsnachweisDokumentDTO;
import com.kcserver.entity.Zahlungsnachweis;
import com.kcserver.entity.ZahlungsnachweisDokument;
import com.kcserver.mapper.ZahlungsnachweisDokumentMapper;
import com.kcserver.repository.abrechnung.ZahlungsnachweisDokumentRepository;
import com.kcserver.repository.abrechnung.ZahlungsnachweisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ZahlungsnachweisDokumentService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10 MB

    private final ZahlungsnachweisDokumentRepository dokumentRepository;
    private final ZahlungsnachweisRepository zahlungsnachweisRepository;
    private final ZahlungsnachweisDokumentMapper mapper;

    /**
     * Alle Dokumente eines Zahlungsnachweises.
     */
    @Transactional(readOnly = true)
    public List<ZahlungsnachweisDokumentDTO> findAll(
            Long veranstaltungId,
            Long zahlungsnachweisId
    ) {

        getZahlungsnachweis(
                veranstaltungId,
                zahlungsnachweisId
        );

        return mapper.toDto(
                dokumentRepository
                        .findByZahlungsnachweisIdOrderByReihenfolgeAsc(
                                zahlungsnachweisId
                        )
        );
    }

    /**
     * Dokument hochladen.
     */
    public ZahlungsnachweisDokumentDTO upload(
            Long veranstaltungId,
            Long zahlungsnachweisId,
            MultipartFile file
    ) {

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Keine Datei ausgewählt."
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Datei ist größer als 10 MB."
            );
        }

        String mimeType = file.getContentType();

        if (mimeType != null) {
            mimeType = mimeType.toLowerCase();
        }

        boolean erlaubt =
                MediaType.APPLICATION_PDF_VALUE.equals(mimeType)
                        || (mimeType != null && mimeType.startsWith("image/"));

        if (!erlaubt) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Es dürfen nur Bilder oder PDF-Dateien hochgeladen werden."
            );
        }

        Zahlungsnachweis zahlungsnachweis =
                getZahlungsnachweis(
                        veranstaltungId,
                        zahlungsnachweisId
                );

        ZahlungsnachweisDokument letztes =
                dokumentRepository
                        .findTopByZahlungsnachweisIdOrderByReihenfolgeDesc(
                                zahlungsnachweisId
                        );

        int reihenfolge =
                letztes == null
                        ? 1
                        : letztes.getReihenfolge() + 1;

        ZahlungsnachweisDokument dokument =
                createDokument(
                        file,
                        mimeType,
                        reihenfolge
                );

        zahlungsnachweis.addDokument(dokument);

        zahlungsnachweisRepository.flush();

        return mapper.toDto(dokument);
    }

    /**
     * Dokument laden.
     *
     * Wird für Download und Preview verwendet.
     */
    @Transactional(readOnly = true)
    public ZahlungsnachweisDokument get(
            Long veranstaltungId,
            Long zahlungsnachweisId,
            Long dokumentId
    ) {

        Zahlungsnachweis zahlungsnachweis =
                getZahlungsnachweis(
                        veranstaltungId,
                        zahlungsnachweisId
                );

        return dokumentRepository
                .findById(dokumentId)
                .filter(dokument ->
                        dokument.getZahlungsnachweis()
                                .getId()
                                .equals(zahlungsnachweis.getId())
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Dokument nicht gefunden."
                        )
                );
    }

    /**
     * Dokument löschen.
     */
    public void delete(
            Long veranstaltungId,
            Long zahlungsnachweisId,
            Long dokumentId
    ) {

        Zahlungsnachweis zahlungsnachweis =
                getZahlungsnachweis(
                        veranstaltungId,
                        zahlungsnachweisId
                );

        ZahlungsnachweisDokument dokument =
                dokumentRepository
                        .findById(dokumentId)
                        .filter(d ->
                                d.getZahlungsnachweis()
                                        .getId()
                                        .equals(zahlungsnachweis.getId())
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Dokument nicht gefunden."
                                )
                        );

        zahlungsnachweis.removeDokument(dokument);

        zahlungsnachweisRepository.flush();
    }

    private Zahlungsnachweis getZahlungsnachweis(
            Long veranstaltungId,
            Long zahlungsnachweisId
    ) {

        return zahlungsnachweisRepository
                .findByIdAndVeranstaltungId(
                        zahlungsnachweisId,
                        veranstaltungId
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Zahlungsnachweis nicht gefunden."
                        )
                );
    }

    private ZahlungsnachweisDokument createDokument(
            MultipartFile file,
            String mimeType,
            Integer reihenfolge
    ) {

        ZahlungsnachweisDokument dokument =
                new ZahlungsnachweisDokument();

        dokument.setReihenfolge(reihenfolge);

        String dateiname = file.getOriginalFilename();

        if (dateiname == null || dateiname.isBlank()) {
            dateiname = "Dokument";
        }

        dokument.setTitel(dateiname);
        dokument.setOriginalDateiname(dateiname);
        dokument.setMimeType(mimeType);
        dokument.setDateigroesse(file.getSize());

        try {
            dokument.setInhalt(file.getBytes());
        } catch (IOException ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Datei konnte nicht gelesen werden.",
                    ex
            );
        }

        return dokument;
    }
}