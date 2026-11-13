package com.kcserver.kjfp.service.planung;

import com.kcserver.core.entity.Veranstaltung;
import com.kcserver.kjfp.dto.simulation.PlanungsSimulation;
import com.kcserver.kjfp.entity.planung.*;
import com.kcserver.kjfp.enumtype.*;
import com.kcserver.kjfp.mapper.*;
import com.kcserver.kjfp.repository.PlanungRepository;
import com.kcserver.kjfp.service.simulation.SimulationFactory;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import java.math.BigDecimal;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlanungKopierServiceTest {
    private final PlanungKopieMapper mapper = Mappers.getMapper(PlanungKopieMapper.class);

    private Planung vorlage() throws Exception {
        Planung p = new Planung();
        // Alle skalaren Fachfelder belegen: zukünftige Ergänzungen werden mitgeprüft.
        int n = 1;
        for (var property : java.beans.Introspector.getBeanInfo(Planung.class).getPropertyDescriptors()) {
            if (property.getWriteMethod() == null) continue;
            if (property.getPropertyType() == Integer.class) property.getWriteMethod().invoke(p,n++);
            if (property.getPropertyType() == BigDecimal.class) property.getWriteMethod().invoke(p,BigDecimal.valueOf(n++).add(new BigDecimal("0.25")));
            if (property.getPropertyType() == boolean.class) property.getWriteMethod().invoke(p,true);
        }
        p.setId(4L); p.setStatus(PlanungsStatus.EINGEREICHT);
        p.setVeranstaltung(new Veranstaltung());
        var position = new PlanungPosition(); position.setId(6L); position.setKategorie(FinanzKategorie.KULTUR);
        position.setMenge(new BigDecimal("2")); position.setEinheit("Eintritte"); position.setEinzelpreis(new BigDecimal("35"));
        position.setBetrag(new BigDecimal("70")); position.setKommentar("Ausflug"); p.addPosition(position);
        return p;
    }
    @Test void allParametersAndPositionsCopiedWithFreshIdentity() throws Exception {
        Planung source=vorlage(), copy=mapper.kopie(source);
        assertThat(copy).usingRecursiveComparison().ignoringFields("id","status","initialisiert","veranstaltung","positionen").isEqualTo(source);
        assertThat(copy.getId()).isNull(); assertThat(copy.getVeranstaltung()).isNull();
        assertThat(copy.isInitialisiert()).isTrue(); assertThat(copy.getStatus()).isEqualTo(PlanungsStatus.IN_BEARBEITUNG);
        assertThat(copy.getPositionen()).hasSize(1);
        var position=copy.getPositionen().getFirst();
        assertThat(position).usingRecursiveComparison().ignoringFields("id","planung").isEqualTo(source.getPositionen().getFirst());
        assertThat(position.getId()).isNull(); assertThat(position.getPlanung()).isNull();
        position.setKommentar("Neue Planung"); copy.setKultur(BigDecimal.TEN);
        assertThat(source.getPositionen().getFirst().getKommentar()).isEqualTo("Ausflug");
        assertThat(source.getKultur()).isNotEqualByComparingTo(BigDecimal.TEN);
    }
    @Test void saveIndependentEditablePlanAndRecalculateWithoutLosingManualPositions() throws Exception {
        var repository=mock(PlanungRepository.class); var factory=mock(SimulationFactory.class);
        var calculation=mock(PlanungBerechnungService.class);
        when(calculation.berechneKultur(any(Planung.class))).thenReturn(new BigDecimal("123.45"));
        var service=new PlanungKopierService(repository,mapper,factory,new PlanungSimulationMapper(mock(VeranstaltungsInfoMapper.class)),new PlanungAutomatikService(calculation));
        var source=vorlage(); var legacy=new PlanungPosition(); legacy.setKategorie(FinanzKategorie.SONSTIGE_EINNAHMEN); legacy.setBetrag(new BigDecimal("80")); source.addPosition(legacy);
        when(repository.findByVeranstaltungIdWithPositionen(4L)).thenReturn(Optional.of(source));
        var target=new Veranstaltung(); target.setId(99L);
        service.kopieren(4L,target);
        var captor=org.mockito.ArgumentCaptor.forClass(Planung.class); verify(repository).save(captor.capture());
        var saved=captor.getValue();
        assertThat(saved.getVeranstaltung()).isSameAs(target); assertThat(saved.istInBearbeitung()).isTrue();
        assertThat(saved.getGeplanteTeilnehmerDivers()).isEqualTo(source.getGeplanteTeilnehmerDivers());
        assertThat(saved.getPositionen()).allSatisfy(p -> {assertThat(p.getId()).isNull();assertThat(p.getPlanung()).isSameAs(saved);});
        var culture=saved.getPositionen().stream().filter(p -> p.getKategorie()==FinanzKategorie.KULTUR).findFirst().orElseThrow();
        assertThat(culture.getBetrag()).isEqualByComparingTo("123.45"); assertThat(culture.getKommentar()).isEqualTo("Ausflug");
        assertThat(saved.getPositionen()).anySatisfy(p -> {assertThat(p.getKategorie()).isEqualTo(FinanzKategorie.SONSTIGE_EINNAHMEN);assertThat(p.getBetrag()).isEqualByComparingTo("80");});
        assertThat(source.istEingereicht()).isTrue(); assertThat(source.getPositionen().getFirst().getBetrag()).isEqualByComparingTo("70");
    }
    @Test void missingSourcePlanCreatesInitializedDefaults() {
        var repository=mock(PlanungRepository.class); var factory=mock(SimulationFactory.class);var target=new Veranstaltung();
        when(repository.findByVeranstaltungIdWithPositionen(4L)).thenReturn(Optional.empty());
        when(factory.fromVeranstaltung(target)).thenReturn(PlanungsSimulation.builder().teilnehmer(14).mitarbeiter(3).kultur(new BigDecimal("45")).build());
        var service=new PlanungKopierService(repository,mapper,factory,new PlanungSimulationMapper(mock(VeranstaltungsInfoMapper.class)),new PlanungAutomatikService(mock(PlanungBerechnungService.class)));
        service.kopieren(4L,target);
        var captor=org.mockito.ArgumentCaptor.forClass(Planung.class);verify(repository).save(captor.capture());
        assertThat(captor.getValue().getTeilnehmer()).isEqualTo(14); assertThat(captor.getValue().getMitarbeiter()).isEqualTo(3);
        assertThat(captor.getValue().isInitialisiert()).isTrue(); assertThat(captor.getValue().istInBearbeitung()).isTrue();
        assertThat(captor.getValue().getPositionen()).isNotEmpty();
    }
}
