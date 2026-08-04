package com.kcserver.abrechnung;

import com.kcserver.dto.abrechnung.AbrechnungBelegCreateDTO;
import com.kcserver.dto.abrechnung.AbrechnungBelegDTO;
import com.kcserver.dto.abrechnung.BelegDokumentDTO;
import com.kcserver.entity.BelegDokument;
import com.kcserver.integration.AbstractFinanzIntegrationTest;
import com.kcserver.service.abrechnung.BelegDokumentService;
import com.kcserver.service.abrechnung.AbrechnungBelegService;
import com.kcserver.service.finanz.FinanzGruppeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class BelegDokumentIntegrationTest
        extends AbstractFinanzIntegrationTest {

    @Autowired
    private BelegDokumentService dokumentService;

    @Autowired
    private AbrechnungBelegService belegService;

    @Autowired
    private FinanzGruppeService finanzGruppeService;

    Long veranstaltungId;
    Long belegId;

    @BeforeEach
    void setup() {

        veranstaltungId = createTestVeranstaltung();

        createOpenAbrechnung(veranstaltungId);

        Long teilnehmerId = createTeilnehmer(
                veranstaltungRepository.findById(veranstaltungId).orElseThrow(),
                null
        ).getId();

        finanzGruppeService.assignKuerzel(
                veranstaltungId,
                teilnehmerId,
                "TEST"
        );

        AbrechnungBelegCreateDTO dto = new AbrechnungBelegCreateDTO();
        dto.setDatum(LocalDate.now());
        dto.setBeschreibung("Integrationstest");
        dto.setKuerzel("TEST");

        AbrechnungBelegDTO beleg =
                belegService.createBeleg(veranstaltungId, dto);

        belegId = beleg.getId();
    }

    @Test
    void shouldUploadDocument() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "rechnung.pdf",
                        "application/pdf",
                        "Hallo".getBytes()
                );

        BelegDokumentDTO dto =
                dokumentService.upload(belegId, file);

        assertThat(dto).isNotNull();
        assertThat(dto.getTitel()).isEqualTo("rechnung.pdf");

        List<BelegDokumentDTO> docs =
                dokumentService.findAll(belegId);

        assertThat(docs).hasSize(1);
    }

    @Test
    void shouldDownloadDocument() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "rechnung.pdf",
                        "application/pdf",
                        "Hallo".getBytes()
                );

        BelegDokumentDTO dto =
                dokumentService.upload(belegId, file);

        System.out.println("DTO-ID = " + dto.getId());

        List<BelegDokumentDTO> docs =
                dokumentService.findAll(belegId);

        System.out.println("Liste-ID = " + docs.getFirst().getId());

        BelegDokument dokument =
                dokumentService.findById(dto.getId());

        assertThat(dokument.getOriginalDateiname())
                .isEqualTo("rechnung.pdf");

        assertThat(dokument.getInhalt())
                .containsExactly("Hallo".getBytes());
    }

    @Test
    void shouldDeleteDocument() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "rechnung.pdf",
                        "application/pdf",
                        "Hallo".getBytes()
                );

        BelegDokumentDTO dto =
                dokumentService.upload(belegId, file);

        dokumentService.delete(dto.getId());

        assertThat(dokumentService.findAll(belegId))
                .isEmpty();
    }

    @Test
    void shouldKeepUploadOrder() {

        dokumentService.upload(
                belegId,
                new MockMultipartFile(
                        "file",
                        "a.pdf",
                        "application/pdf",
                        new byte[]{1}
                ));

        dokumentService.upload(
                belegId,
                new MockMultipartFile(
                        "file",
                        "b.pdf",
                        "application/pdf",
                        new byte[]{2}
                ));

        dokumentService.upload(
                belegId,
                new MockMultipartFile(
                        "file",
                        "c.pdf",
                        "application/pdf",
                        new byte[]{3}
                ));

        List<BelegDokumentDTO> docs =
                dokumentService.findAll(belegId);

        assertThat(docs).hasSize(3);

        assertThat(docs)
                .extracting(BelegDokumentDTO::getReihenfolge)
                .containsExactly(1, 2, 3);
    }
}