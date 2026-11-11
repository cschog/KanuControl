package com.kcserver.vermietung.controller;

import com.kcserver.vermietung.exception.BuchungLoeschbestaetigungException;
import com.kcserver.vermietung.service.BuchungService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BuchungLoeschenControllerTest {
    @Test void ohneZusatzbestaetigungKommtEinAuswertbarerKonflikt() throws Exception {
        var service = mock(BuchungService.class);
        doThrow(new BuchungLoeschbestaetigungException(2)).when(service).delete(19L, false);
        var mvc = MockMvcBuilders.standaloneSetup(new BuchungController(service)).build();
        mvc.perform(delete("/api/vermietung/buchungen/19"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(BuchungLoeschbestaetigungException.CODE))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("2 Abrechnungsdokument")));
        verify(service).delete(19L, false);
    }

    @Test void ausdruecklicheBestaetigungWirdAnDenServiceWeitergereicht() throws Exception {
        var service = mock(BuchungService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new BuchungController(service)).build();
        mvc.perform(delete("/api/vermietung/buchungen/19")
                        .param("archivierteAbrechnungenBestaetigt", "true"))
                .andExpect(status().isNoContent());
        verify(service).delete(19L, true);
    }
}
