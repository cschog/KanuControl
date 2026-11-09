package com.kcserver.vermietung.service;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.repository.VorlagenVersandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
@Service @RequiredArgsConstructor
public class VorlagenArchivService {
    private final VorlagenVersandRepository archiv;
    /** Commit the exact attachment before SMTP; a crash leaves an explicitly unconfirmed attempt. */
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public String beginnen(Buchung b,HomepageStatusMailService.ReservierungMail mail){
        VorlagenService.tenant();var a=new VorlagenVersand();a.setBuchungId(b.getId());a.setMietobjektId(b.getMietobjekt().getId());a.setVorgang("RESERVIERUNG");a.setPdf(mail.pdf().clone());a.setMetadaten(mail.metadaten());return archiv.saveAndFlush(a).getId();
    }
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void ergebnis(String id,boolean erfolg,String fehler){
        VorlagenService.tenant();var a=archiv.findById(id).orElseThrow();a.setErgebnis(erfolg?"SMTP_BESTAETIGT":"FEHLER_ODER_UNKLAR");a.setFehler(fehler);if(erfolg)a.setSmtpAm(java.time.Instant.now());
    }
}
