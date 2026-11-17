package com.kcserver.kjfp.usecase;

import com.kcserver.core.tenancy.*;
import com.kcserver.kjfp.dto.abrechnung.AbrechnungBelegCreateDTO;
import com.kcserver.kjfp.enumtype.Zahlungsweg;
import com.kcserver.kjfp.service.abrechnung.DokumentService;
import com.kcserver.kjfp.service.zahlungsnachweis.ZahlungsnachweisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.mock.web.MockMultipartFile;
import javax.sql.DataSource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
class MandantenGrenzenUseCaseTest extends KjfpUseCaseBasis {
    @Autowired TenantSchemaProvisioner provisioner;
    @Autowired PlatformTransactionManager manager;
    @Autowired DataSource dataSource;
    @Autowired DokumentService dokumente;
    @Autowired ZahlungsnachweisService zahlungen;
    @Test void zweiterMandantKannZahlungenUndDokumenteDesErstenNichtLesenOderAendern()throws Exception {
        TestTransaction.end();String fremd="kjfp_test_"+UUID.randomUUID().toString().replace("-","");provisioner.createFromBaseline(fremd);
        try {
            TenantContext.setCurrentTenant(tenant());
            long[] ids=new TransactionTemplate(manager).execute(status->{
                var v=event();var konto=finanzGruppeService.create(v.getId(),"A");var t=person(v,konto);em.flush();createOpenAbrechnung(v.getId());
                var b=new AbrechnungBelegCreateDTO();b.setKuerzel("A");b.setDatum(START);long beleg=abrechnungBelegService.createBeleg(v.getId(),b).getId();
                var out=new ByteArrayOutputStream();try{ImageIO.write(new BufferedImage(10,10,BufferedImage.TYPE_INT_RGB),"png",out);}catch(Exception ex){throw new AssertionError(ex);}
                long doc=dokumente.uploadForBeleg(beleg,new MockMultipartFile("file","q.png","image/png",out.toByteArray()),com.kcserver.kjfp.enumtype.ReferenzObjekt.DIN_A4).getId();
                long zahlung=zahlungen.create(v.getId(),zahlung("25",Zahlungsweg.UEBERWEISUNG,konto,t)).getId();return new long[]{v.getId(),beleg,doc,zahlung};
            });
            String docUrl="/api/belege/"+ids[1]+"/dokumente/"+ids[2],zahlungUrl="/api/veranstaltungen/"+ids[0]+"/zahlungsnachweise/"+ids[3];
            TenantContext.setCurrentTenant(fremd);
            mockMvc.perform(get(docUrl).header("X-Tenant",fremd)).andExpect(status().isNotFound());
            TenantContext.setCurrentTenant(fremd);
            mockMvc.perform(put(docUrl+"/ausrichtung").header("X-Tenant",fremd).contentType("application/json").content("{\"version\":0,\"drehungen\":[180]}")) .andExpect(status().isNotFound());
            TenantContext.setCurrentTenant(fremd);
            mockMvc.perform(get(zahlungUrl).header("X-Tenant",fremd)).andExpect(status().isNotFound());
            TenantContext.setCurrentTenant(fremd);
            mockMvc.perform(delete(zahlungUrl).header("X-Tenant",fremd)).andExpect(status().isNotFound());
            TenantContext.setCurrentTenant(tenant());
            mockMvc.perform(get(docUrl+"/ausrichtung").header("X-Tenant",tenant())).andExpect(status().isOk()).andExpect(jsonPath("$.version").value(0)).andExpect(jsonPath("$.drehungen[0]").value(0));
            TenantContext.setCurrentTenant(tenant());mockMvc.perform(get(zahlungUrl).header("X-Tenant",tenant())).andExpect(status().isOk()).andExpect(jsonPath("$.data.betrag").value(25));
        }finally{TenantContext.clear();try(var c=dataSource.getConnection();var sql=c.createStatement()){sql.execute("DROP SCHEMA "+fremd+" CASCADE");}TenantContext.setCurrentTenant(tenant());}
    }
}
