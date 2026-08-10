package com.kcserver.abrechnung;

import com.kcserver.dto.abrechnung.BelegDokumentDTO;
import com.kcserver.entity.AbrechnungBeleg;
import com.kcserver.entity.BelegDokument;
import com.kcserver.mapper.BelegDokumentMapper;
import com.kcserver.repository.abrechnung.AbrechnungBelegRepository;
import com.kcserver.repository.abrechnung.BelegDokumentRepository;
import com.kcserver.service.abrechnung.BelegDokumentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BelegDokumentServiceTest {

    @Mock
    private BelegDokumentRepository dokumentRepository;

    @Mock
    private AbrechnungBelegRepository belegRepository;

    @Mock
    private BelegDokumentMapper mapper;

    @InjectMocks
    private BelegDokumentService service;

    @Test
    void shouldUploadPdf() {

        AbrechnungBeleg beleg = new AbrechnungBeleg();

        when(belegRepository.findById(1L))
                .thenReturn(java.util.Optional.of(beleg));

        when(mapper.toDto(any(BelegDokument.class)))
                .thenReturn(new BelegDokumentDTO());

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "rechnung.pdf",
                        "application/pdf",
                        "Hallo".getBytes()
                );

        service.upload(1L, file);

        verify(belegRepository).flush();

        AbrechnungBeleg gespeichert = beleg;

        assertThat(gespeichert.getDokumente())
                .hasSize(1);

        BelegDokument dokument =
                gespeichert.getDokumente().getFirst();

        assertThat(dokument.getTitel())
                .isEqualTo("rechnung.pdf");

        assertThat(dokument.getMimeType())
                .isEqualTo("application/pdf");

        assertThat(dokument.getDateigroesse())
                .isEqualTo(file.getSize());

        assertThat(dokument.getReihenfolge())
                .isEqualTo(1);
    }

    @Test
    void shouldUploadImage() {

        AbrechnungBeleg beleg = new AbrechnungBeleg();

        when(belegRepository.findById(1L))
                .thenReturn(java.util.Optional.of(beleg));

        when(mapper.toDto(any(BelegDokument.class)))
                .thenReturn(new BelegDokumentDTO());

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "bild.jpg",
                        "image/jpeg",
                        new byte[]{1,2,3}
                );

        service.upload(1L, file);

        verify(belegRepository).flush();
    }

    @Test
    void shouldAssignFirstSequenceNumber() {

        AbrechnungBeleg beleg = new AbrechnungBeleg();

        when(belegRepository.findById(1L))
                .thenReturn(java.util.Optional.of(beleg));

        when(mapper.toDto(any(BelegDokument.class)))
                .thenReturn(new BelegDokumentDTO());

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "a.pdf",
                        "application/pdf",
                        new byte[]{1}
                );

        service.upload(1L, file);

        assertThat(beleg.getDokumente())
                .first()
                .extracting(BelegDokument::getReihenfolge)
                .isEqualTo(1);
    }

    @Test
    void shouldIncrementSequenceNumber() {

        AbrechnungBeleg beleg = new AbrechnungBeleg();

        BelegDokument alt = new BelegDokument();
        alt.setReihenfolge(3);

        when(belegRepository.findById(1L))
                .thenReturn(java.util.Optional.of(beleg));

        when(dokumentRepository
                .findTopByBelegIdOrderByReihenfolgeDesc(1L))
                .thenReturn(alt);

        when(mapper.toDto(any(BelegDokument.class)))
                .thenReturn(new BelegDokumentDTO());

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "test.pdf",
                        "application/pdf",
                        new byte[]{1}
                );

        service.upload(1L, file);

        assertThat(beleg.getDokumente())
                .first()
                .extracting(BelegDokument::getReihenfolge)
                .isEqualTo(4);
    }

    @Test
    void shouldUseFallbackFilenameWhenOriginalFilenameMissing() {

        AbrechnungBeleg beleg = new AbrechnungBeleg();

        when(belegRepository.findById(1L))
                .thenReturn(java.util.Optional.of(beleg));

        when(mapper.toDto(any(BelegDokument.class)))
                .thenReturn(new BelegDokumentDTO());

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "",
                        "application/pdf",
                        new byte[]{1}
                );

        service.upload(1L, file);

        assertThat(beleg.getDokumente())
                .first()
                .extracting(BelegDokument::getTitel)
                .isEqualTo("Dokument");
    }

    @Test
    void shouldRejectNullFile() {

        assertThatThrownBy(() ->
                service.upload(1L, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Keine Datei");
    }

    @Test
    void shouldRejectEmptyFile() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "leer.pdf",
                        "application/pdf",
                        new byte[0]
                );

        assertThatThrownBy(() ->
                service.upload(1L, file))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Keine Datei");
    }

    @Test
    void shouldRejectUnsupportedMimeType() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "virus.exe",
                        "application/octet-stream",
                        new byte[]{1, 2, 3}
                );

        assertThatThrownBy(() ->
                service.upload(1L, file))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Bilder oder PDF");
    }

    @Test
    void shouldRejectFileLargerThan10Mb() {

        byte[] data = new byte[10 * 1024 * 1024 + 1];

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "gross.pdf",
                        "application/pdf",
                        data
                );

        assertThatThrownBy(() ->
                service.upload(1L, file))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("10 MB");
    }

    @Test
    void shouldReturn404WhenBelegDoesNotExist() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "rechnung.pdf",
                        "application/pdf",
                        new byte[]{1}
                );

        when(belegRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.upload(99L, file))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Beleg nicht gefunden");
    }

    @Test
    void shouldReturn500WhenReadingFileFails() throws Exception {

        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(100L);
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getOriginalFilename()).thenReturn("rechnung.pdf");

        when(belegRepository.findById(1L))
                .thenReturn(Optional.of(new AbrechnungBeleg()));

        when(file.getBytes())
                .thenThrow(new IOException("kaputt"));

        assertThatThrownBy(() ->
                service.upload(1L, file))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Datei konnte nicht gelesen werden");
    }

    @Test
    void shouldReturnDocument() {

        BelegDokument dokument = new BelegDokument();

        when(dokumentRepository.findById(1L))
                .thenReturn(Optional.of(dokument));

        assertThat(service.findById(1L))
                .isSameAs(dokument);
    }

    @Test
    void shouldReturn404WhenDocumentDoesNotExist() {

        when(dokumentRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.findById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Dokument nicht gefunden");
    }

    @Test
    void shouldDeleteDocument() {

        AbrechnungBeleg beleg = new AbrechnungBeleg();

        BelegDokument dokument = new BelegDokument();
        dokument.setBeleg(beleg);

        beleg.addDokument(dokument);

        when(dokumentRepository.findById(1L))
                .thenReturn(Optional.of(dokument));

        service.delete(1L);

        verify(belegRepository).flush();

        assertThat(beleg.getDokumente())
                .isEmpty();
    }

    @Test
    void shouldReturn404WhenDeletingUnknownDocument() {

        when(dokumentRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.delete(1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Dokument nicht gefunden");
    }

    @Test
    void shouldReturnAllDocumentsOrdered() {

        BelegDokument d1 = new BelegDokument();
        d1.setReihenfolge(1);

        BelegDokument d2 = new BelegDokument();
        d2.setReihenfolge(2);

        List<BelegDokument> dokumente = List.of(d1, d2);
        List<BelegDokumentDTO> dtos = List.of(
                new BelegDokumentDTO(),
                new BelegDokumentDTO()
        );

        when(dokumentRepository.findByBelegIdOrderByReihenfolgeAsc(1L))
                .thenReturn(dokumente);

        when(mapper.toDto(dokumente))
                .thenReturn(dtos);

        List<BelegDokumentDTO> result =
                service.findAll(1L);

        assertThat(result)
                .hasSize(2)
                .isSameAs(dtos);

        verify(dokumentRepository)
                .findByBelegIdOrderByReihenfolgeAsc(1L);

        verify(mapper)
                .toDto(dokumente);
    }

    @Test
    void shouldReturnMappedDtoAfterUpload() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "rechnung.pdf",
                        "application/pdf",
                        new byte[]{1, 2, 3}
                );

        AbrechnungBeleg beleg = new AbrechnungBeleg();

        BelegDokumentDTO dto = new BelegDokumentDTO();
        dto.setTitel("rechnung.pdf");

        when(belegRepository.findById(1L))
                .thenReturn(Optional.of(beleg));

        when(dokumentRepository.findTopByBelegIdOrderByReihenfolgeDesc(1L))
                .thenReturn(null);

        when(mapper.toDto(any(BelegDokument.class)))
                .thenReturn(dto);

        BelegDokumentDTO result =
                service.upload(1L, file);

        assertThat(result)
                .isSameAs(dto);

        verify(mapper)
                .toDto(any(BelegDokument.class));
    }
}