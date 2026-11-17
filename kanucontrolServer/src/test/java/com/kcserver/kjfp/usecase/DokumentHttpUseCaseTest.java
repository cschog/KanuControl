package com.kcserver.kjfp.usecase;

import com.kcserver.kjfp.dto.abrechnung.AbrechnungBelegCreateDTO;
import com.kcserver.kjfp.repository.abrechnung.DokumentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
class DokumentHttpUseCaseTest extends KjfpUseCaseBasis {
    @Autowired com.fasterxml.jackson.databind.ObjectMapper json;
    @Autowired DokumentRepository dokumente;
    Long beleg(Long id) {
        var b=new AbrechnungBelegCreateDTO();b.setKuerzel("A");b.setDatum(START);return abrechnungBelegService.createBeleg(id,b).getId();
    }
    @Test void uploadEchteVersionierungDownloadUndFremdeBelegzuordnung()throws Exception {
        var id=event().getId();finanzGruppeService.create(id,"A");createOpenAbrechnung(id);var owner=beleg(id);var wrong=beleg(id);
        var out=new ByteArrayOutputStream();ImageIO.write(new BufferedImage(100,60,BufferedImage.TYPE_INT_RGB),"png",out);byte[] original=out.toByteArray();
        var response=mockMvc.perform(multipart("/api/belege/{b}/dokumente",owner).file(new MockMultipartFile("file","quittung.png","image/png",original)).param("referenzObjekt","DIN_A4")).andExpect(status().isOk()).andReturn();
        long doc=json.readTree(response.getResponse().getContentAsByteArray()).get("id").asLong();neuLaden();String url="/api/belege/"+owner+"/dokumente/"+doc;
        mockMvc.perform(get(url+"/ausrichtung")).andExpect(status().isOk()).andExpect(jsonPath("$.version").value(0)).andExpect(jsonPath("$.drehungen[0]").value(0));
        mockMvc.perform(put(url+"/ausrichtung").contentType("application/json").content("{\"version\":0,\"drehungen\":[90]}")) .andExpect(status().isOk()).andExpect(jsonPath("$.version").value(1));neuLaden();
        assertThat(dokumente.findById(doc).orElseThrow().getInhalt()).isEqualTo(original);
        mockMvc.perform(get(url+"/ausrichtung")).andExpect(jsonPath("$.drehungen[0]").value(90));
        var download=mockMvc.perform(get(url)).andExpect(status().isOk()).andExpect(content().contentType("image/png")).andExpect(header().string("Cache-Control","no-store")).andReturn().getResponse().getContentAsByteArray();
        var image=ImageIO.read(new ByteArrayInputStream(download));assertThat(image.getWidth()).isEqualTo(60);assertThat(image.getHeight()).isEqualTo(100);
        mockMvc.perform(get("/api/belege/"+wrong+"/dokumente/"+doc)).andExpect(status().isNotFound());
        mockMvc.perform(delete(url)).andExpect(status().isOk());neuLaden();assertThat(dokumente.findById(doc)).isEmpty();
    }
    @Test void veralteteAusrichtungWirdDurchEchteJpaVersionAbgewiesen()throws Exception {
        var id=event().getId();finanzGruppeService.create(id,"A");createOpenAbrechnung(id);var owner=beleg(id);
        var out=new ByteArrayOutputStream();ImageIO.write(new BufferedImage(10,10,BufferedImage.TYPE_INT_RGB),"png",out);
        var response=mockMvc.perform(multipart("/api/belege/{b}/dokumente",owner).file(new MockMultipartFile("file","q.png","image/png",out.toByteArray())).param("referenzObjekt","DIN_A4")).andReturn();long doc=json.readTree(response.getResponse().getContentAsByteArray()).get("id").asLong();String url="/api/belege/"+owner+"/dokumente/"+doc+"/ausrichtung";
        mockMvc.perform(put(url).contentType("application/json").content("{\"version\":0,\"drehungen\":[180]}")) .andExpect(status().isOk());neuLaden();
        mockMvc.perform(put(url).contentType("application/json").content("{\"version\":0,\"drehungen\":[0]}")) .andExpect(status().isConflict());
    }
    @Test void zuGrosserUploadWirdVorDemSpeichernAbgewiesen()throws Exception {
        var id=event().getId();finanzGruppeService.create(id,"A");createOpenAbrechnung(id);var owner=beleg(id);
        mockMvc.perform(multipart("/api/belege/{b}/dokumente",owner).file(new MockMultipartFile("file","zu-gross.pdf","application/pdf",new byte[10*1024*1024+1])).param("referenzObjekt","DIN_A4")).andExpect(status().isBadRequest());
    }
}
