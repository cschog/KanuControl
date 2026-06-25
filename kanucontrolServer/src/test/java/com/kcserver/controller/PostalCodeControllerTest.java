package com.kcserver.controller;

import com.kcserver.dto.postalcode.PostalCodeLookupResponse;
import com.kcserver.enumtype.CountryCode;
import com.kcserver.service.postalcode.PostalCodeService;

import com.kcserver.support.tenant.AbstractTenantIntegrationTest;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;


import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PostalCodeControllerTest extends AbstractTenantIntegrationTest {

    @MockitoBean
    private PostalCodeService postalCodeService;

    @Test
    void shouldLookupPostalCode() throws Exception {

        PostalCodeLookupResponse response =
                new PostalCodeLookupResponse(
                        "50226",
                        "Frechen",
                        CountryCode.DE
                );

        when(postalCodeService.lookup(
                CountryCode.DE,
                "50226"
        )).thenReturn(Optional.of(response));

        mockMvc.perform(
                        req(get("/api/postal-codes/lookup")
                                .param("country", "DE")
                                .param("postalCode", "50226"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.postalCode").value("50226"))
                .andExpect(jsonPath("$.data.city").value("Frechen"))
                .andExpect(jsonPath("$.data.countryCode").value("DE"));
    }
}