package com.kcserver.kjfp.usecase;

import com.kcserver.core.tenancy.TenantContext;
import com.kcserver.kjfp.enumtype.Zahlungsweg;
import com.kcserver.kjfp.service.zahlungsnachweis.ZahlungsnachweisService;
import com.kcserver.kjfp.repository.zahlungsnachweis.ZahlungsnachweisRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

@Transactional
class RueckzahlungParallelUseCaseTest extends KjfpUseCaseBasis {
    @Autowired ZahlungsnachweisService zahlungen;
    @Autowired ZahlungsnachweisRepository repository;
    @Autowired PlatformTransactionManager manager;
    @Test void zweiTransaktionenKoennenDieselbeUeberzahlungNichtDoppeltErstatten()throws Exception {
        var v=event();var konto=finanzGruppeService.create(v.getId(),"A");var t=person(v,konto);em.flush();
        long event=v.getId(),id=zahlungen.create(event,zahlung("150",Zahlungsweg.UEBERWEISUNG,konto,t)).getId();
        TestTransaction.flagForCommit();TestTransaction.end();
        var written=new CountDownLatch(1);var release=new CountDownLatch(1);var started=new CountDownLatch(1);
        var pool=Executors.newFixedThreadPool(2);
        try {
            var first=pool.submit(()->{TenantContext.setCurrentTenant(tenant());try{return new TransactionTemplate(manager).execute(status->{zahlungen.rueckzahlungTeilnehmerbeitrag(event,id,new BigDecimal("40"),null,Zahlungsweg.UEBERWEISUNG);written.countDown();await(release);return 200;});}finally{TenantContext.clear();}});
            assertThat(written.await(10,TimeUnit.SECONDS)).isTrue();
            var second=pool.submit(()->{TenantContext.setCurrentTenant(tenant());started.countDown();try{new TransactionTemplate(manager).executeWithoutResult(status->zahlungen.rueckzahlungTeilnehmerbeitrag(event,id,new BigDecimal("40"),null,Zahlungsweg.UEBERWEISUNG));return 200;}catch(ResponseStatusException ex){return ex.getStatusCode().value();}finally{TenantContext.clear();}});
            assertThat(started.await(10,TimeUnit.SECONDS)).isTrue();
            // Ohne Sperre kann die zweite Transaktion bereits vor dem ersten Commit unberechtigt speichern.
            Integer early=null;try{early=second.get(500,TimeUnit.MILLISECONDS);}catch(TimeoutException expected){/* wartet auf die Zeilensperre */}
            release.countDown();assertThat(first.get(10,TimeUnit.SECONDS)).isEqualTo(200);
            assertThat(early==null?second.get(10,TimeUnit.SECONDS):early).isEqualTo(409);
            TenantContext.setCurrentTenant(tenant());
            var sum=new TransactionTemplate(manager).execute(status->repository.sumRueckzahlungenByUrspruenglichemZahlungsnachweisId(id));assertThat(sum).isEqualByComparingTo("40");
        }finally{release.countDown();pool.shutdownNow();assertThat(pool.awaitTermination(10,TimeUnit.SECONDS)).isTrue();}
    }
    static void await(CountDownLatch latch){try{if(!latch.await(10,TimeUnit.SECONDS))throw new AssertionError("Keine Freigabe");}catch(InterruptedException ex){Thread.currentThread().interrupt();throw new AssertionError(ex);}}
}
