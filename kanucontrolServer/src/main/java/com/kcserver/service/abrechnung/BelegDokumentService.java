package com.kcserver.service.abrechnung;

import com.kcserver.dto.abrechnung.BelegDokumentDTO;
import com.kcserver.entity.AbrechnungBeleg;
import com.kcserver.entity.BelegDokument;
import com.kcserver.mapper.BelegDokumentMapper;
import com.kcserver.repository.AbrechnungBelegRepository;
import com.kcserver.repository.BelegDokumentRepository;
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
public class BelegDokumentService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10 MB

    private final BelegDokumentRepository belegDokumentRepository;
    private final AbrechnungBelegRepository abrechnungBelegRepository;
    private final BelegDokumentMapper belegDokumentMapper;

    /**
     * Alle Dokumente eines Belegs.
     */
    @Transactional(readOnly = true)
    public List<BelegDokumentDTO> findAll(Long belegId) {

        return belegDokumentMapper.toDto(
                belegDokumentRepository.findByBelegIdOrderByReihenfolgeAsc(belegId)
        );
    }

    /**
     * Dokument hochladen.
     */
    public BelegDokumentDTO upload(
            Long belegId,
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

        AbrechnungBeleg beleg = getBeleg(belegId);

        BelegDokument letztes =
                belegDokumentRepository.findTopByBelegIdOrderByReihenfolgeDesc(belegId);

        int reihenfolge =
                letztes == null
                        ? 1
                        : letztes.getReihenfolge() + 1;

        BelegDokument dokument =
                createDokument(file, mimeType, reihenfolge);

        beleg.addDokument(dokument);

        abrechnungBelegRepository.flush();

        return belegDokumentMapper.toDto(dokument);
    }

    /**
     * Dokument herunterladen.
     */
    @Transactional(readOnly = true)
    public BelegDokument findById(Long dokumentId) {

        return getDokument(dokumentId);
    }

    /**
     * Dokument löschen.
     */
    public void delete(Long dokumentId) {

        BelegDokument dokument = getDokument(dokumentId);

        AbrechnungBeleg beleg = dokument.getBeleg();

        beleg.removeDokument(dokument);

        abrechnungBelegRepository.flush();
    }

    private AbrechnungBeleg getBeleg(Long belegId) {
        return abrechnungBelegRepository.findById(belegId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Beleg nicht gefunden."
                        ));
    }

    private BelegDokument getDokument(Long dokumentId) {
        return belegDokumentRepository.findById(dokumentId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Dokument nicht gefunden."
                        ));
    }


    private BelegDokument createDokument(
            MultipartFile file,
            String mimeType,
            Integer reihenfolge
    ) {

        BelegDokument dokument = new BelegDokument();

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