package com.kcserver.csv;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.integration.support.AbstractTenantIntegrationTest;
import com.kcserver.testdata.VereinTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CsvImportControllerTest extends AbstractTenantIntegrationTest {

    @Autowired
    ObjectMapper objectMapper;

    Long vereinId;

    @BeforeEach
    void setup() throws Exception {

        VereinTestFactory vereine =
                new VereinTestFactory(mockMvc, objectMapper, tenantAuth());

        vereinId = vereine.createIfNotExists(
                "TEST",
                "Testverein"
        );
    }

    @Test
    void dryRun_import_returnsReport_butPersistsNothing() throws Exception {

        mockMvc.perform(
                        tenantRequest(
                                multipart("/api/csv-import/verein/{id}", vereinId)
                                        .file("csv", load("/import/personen.csv"))
                                        .file("mapping", load("/import/mapping.csv"))
                                        .param("dryRun", "true")
                                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRows").isNumber())
                .andExpect(jsonPath("$.created").isNumber())
                .andExpect(jsonPath("$.errors").isNumber());
    }

    /* =========================
       Helper
       ========================= */

    private byte[] load(String path) throws Exception {

        InputStream is = getClass().getResourceAsStream(path);
        assertThat(is)
                .as("Resource %s muss existieren", path)
                .isNotNull();

        return is.readAllBytes();
    }
    @Test
    void import_withOptionalEmailColumn_succeeds() throws Exception {

        mockMvc.perform(
                        tenantRequest(
                                multipart("/api/csv-import/verein/{id}", vereinId)
                                        .file("csv", load("/import/personen.csv"))
                                        .file("mapping", load("/import/mapping.csv"))
                                        .param("dryRun", "true")
                                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").value(0))
                .andExpect(jsonPath("$.created").isNumber());
    }
}