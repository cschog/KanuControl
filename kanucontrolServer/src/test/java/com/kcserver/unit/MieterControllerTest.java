package com.kcserver.unit;
import com.kcserver.vermietung.controller.MieterController;
import com.kcserver.vermietung.service.MieterService;
import com.kcserver.core.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
class MieterControllerTest {
    MieterService service; MockMvc mvc;
    @BeforeEach void setup(){service=mock(MieterService.class);mvc=MockMvcBuilders.standaloneSetup(new MieterController(service)).setControllerAdvice(new GlobalExceptionHandler()).build();}
    @Test void endgueltigesLoeschenVerwendetEigenenExplizitenEndpunkt() throws Exception{mvc.perform(delete("/api/vermietung/mieter/5/endgueltig")).andExpect(status().isNoContent());verify(service).delete(5L);verify(service,never()).deactivate(any());}
    @Test void alteClientsArchivierenMitDeleteWeiterhinOhneEndgueltigesLoeschen() throws Exception{mvc.perform(delete("/api/vermietung/mieter/5")).andExpect(status().isNoContent());verify(service).deactivate(5L);verify(service,never()).delete(any());}
    @Test void neuerArchivierungsendpunktLoeschtNicht() throws Exception{mvc.perform(put("/api/vermietung/mieter/5/archivieren")).andExpect(status().isNoContent());verify(service).deactivate(5L);verify(service,never()).delete(any());}
    @Test void buchungskonfliktWirdMitVerstaendlicherMeldungZurueckgegeben() throws Exception{doThrow(new ResponseStatusException(HttpStatus.CONFLICT,"Bitte zuerst alle zugehörigen Buchungen löschen.")).when(service).delete(5L);mvc.perform(delete("/api/vermietung/mieter/5/endgueltig")).andExpect(status().isConflict()).andExpect(jsonPath("$.message").value("Bitte zuerst alle zugehörigen Buchungen löschen."));}
    @Test void falscheIbanWirdDirektDemFeldZugeordnetUndNichtGespeichert() throws Exception {
        mvc.perform(post("/api/vermietung/mieter").contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Adler\",\"vorname\":\"Anna\",\"iban\":\"DE123\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors.iban").value(org.hamcrest.Matchers.containsString("IBAN")));
        verifyNoInteractions(service);
    }
    @Test void formatierteGueltigeIbanUndKopierteMailadresseWerdenVorValidierungNormalisiert() throws Exception {
        mvc.perform(post("/api/vermietung/mieter").contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("""
                        {"name":" Adler ","vorname":" Anna ","email":" anna@example.org ","iban":"de89 3704 0044 0532 0130 00"}
                        """))
                .andExpect(status().isCreated());
        verify(service).create(argThat(dto->dto.getName().equals("Adler") && dto.getVorname().equals("Anna")
                && dto.getEmail().equals("anna@example.org") && dto.getIban().equals("DE89370400440532013000")));
    }
    @Test void optionaleEmailUndIbanDuerfenLeerBleiben() throws Exception {
        mvc.perform(post("/api/vermietung/mieter").contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Adler\",\"vorname\":\"Anna\",\"email\":\"\",\"iban\":\"\"}"))
                .andExpect(status().isCreated());
        verify(service).create(any());
    }

}
