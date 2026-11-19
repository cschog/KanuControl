package com.kcserver.unit;

import com.kcserver.core.entity.Person;
import com.kcserver.kjfp.repository.PersonRepository;
import com.kcserver.vermietung.dto.*;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.*;
import com.kcserver.vermietung.repository.*;
import com.kcserver.vermietung.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Exercises the public update path, not a replica of the discount comparison. */
class BuchungRabattSnapshotTest {
    private final BuchungRepository bookings = mock(BuchungRepository.class);
    private final MietbereichRabattRepository discounts = mock(MietbereichRabattRepository.class);
    private final Buchung booking = new Buchung();
    private final BuchungMietbereich position = new BuchungMietbereich();
    private BuchungService service;
    private BuchungDTO dto;

    @BeforeEach void setup() {
        var person = new Person();
        person.setId(1L);
        person.setKjfpSichtbar(true);
        var persons = mock(PersonRepository.class);
        when(persons.findLockedById(1L)).thenReturn(Optional.of(person));
        var object = new Mietobjekt();
        object.setId(7L);
        object.setAnreisezeit(LocalTime.of(16, 0));
        object.setAbreisezeit(LocalTime.of(14, 0));
        object.setDirektbuchungAktiv(true);
        object.setAirbnbAktiv(true);
        var objects = mock(MietobjektRepository.class);
        when(objects.findById(7L)).thenReturn(Optional.of(object));
        var area = new Mietbereich();
        area.setId(3L);
        area.setMietobjekt(object);
        area.setBerechnungsart(Berechnungsart.BUCHUNG);
        area.setAirbnbAktiv(true);
        var areas = mock(MietbereichRepository.class);
        when(areas.findAllById(List.of(3L))).thenReturn(List.of(area));

        booking.setId(24L);
        booking.setBuchungsnummer("2026-024");
        booking.setMieter(person);
        booking.setMietobjekt(object);
        booking.setStatus(Buchungsstatus.ABRECHENBAR);
        booking.setBuchungsquelle(Buchungsquelle.DIREKT);
        booking.setAnreise(LocalDateTime.of(2026, 7, 10, 16, 0));
        booking.setAbreise(LocalDateTime.of(2026, 7, 12, 14, 0));
        booking.setRabattart(Rabattart.INDIVIDUELL);
        booking.setRabattwertTyp(RabattwertTyp.PROZENTUAL);
        booking.setRabattwert(new BigDecimal("10.00"));
        position.setId(8L);
        position.setBuchung(booking);
        position.setMietbereich(area);
        position.setAnzahl(1);
        position.setVereinbarterPreis(new BigDecimal("200.00"));
        position.setBerechnungsart(Berechnungsart.BUCHUNG);
        position.setRabattwertTyp(RabattwertTyp.PROZENTUAL);
        position.setRabattwert(new BigDecimal("10.00"));
        position.setRabattbetrag(new BigDecimal("20.00"));
        booking.getMietbereiche().add(position);
        when(bookings.findLockedById(24L)).thenReturn(Optional.of(booking));
        service = new BuchungService(bookings, mock(MieterRepository.class), null, persons,
                mock(com.kcserver.core.repository.VereinRepository.class), objects, areas,
                mock(MietpreisRepository.class), discounts, null, null,
                mock(VerbrauchsabrechnungService.class), mock(BuchungVerbrauchskostenRepository.class),
                mock(BuchungFinanzbewegungRepository.class), null, org.mockito.Mockito.mock(com.kcserver.vermietung.service.VermietungAenderungsprotokoll.class));
        dto = new BuchungDTO();
        dto.setMieterId(1L);
        dto.setMietobjektId(7L);
        dto.setAnreise(booking.getAnreise());
        dto.setAbreise(booking.getAbreise());
        dto.setRabattart(Rabattart.INDIVIDUELL);
        dto.setRabattwertTyp(RabattwertTyp.PROZENTUAL);
        dto.setRabattwert(new BigDecimal("10.00"));
        var row = new BuchungMietbereichDTO();
        row.setMietbereichId(3L);
        row.setAnzahl(1);
        dto.setPositionen(List.of(row));
    }

    @ParameterizedTest
    @CsvSource({"10,10", "10,10.0", "10,10.00", "10.0,10", "10.0,10.0", "10.0,10.00",
            "10.00,10", "10.00,10.0", "10.00,10.00"})
    void equalValuesKeepSnapshotAfterCatalogEligibilityWasRemoved(String stored, String requested) {
        booking.setRabattwert(new BigDecimal(stored));
        dto.setRabattwert(new BigDecimal(requested));
        // The area no longer qualifies in the current catalog. Recalculation would erase its discount.
        when(discounts.findByMietbereichIdAndAktivTrue(3L)).thenReturn(List.of());
        service.update(24L, dto);
        assertEquals(RabattwertTyp.PROZENTUAL, position.getRabattwertTyp());
        assertEquals(new BigDecimal("10.00"), position.getRabattwert());
        money("20.00", booking.getRabattbetrag());
        money("180.00", BuchungPreiskalkulation.miete(booking));
        verifyNoInteractions(discounts);
        verify(bookings).flush();
    }

    @Test void changedValueRecalculatesEligiblePosition() {
        when(discounts.findByMietbereichIdAndAktivTrue(3L)).thenReturn(List.of(new MietbereichRabatt()));
        dto.setRabattwert(new BigDecimal("20"));
        service.update(24L, dto);
        money("20", position.getRabattwert());
        money("40.00", booking.getRabattbetrag());
        money("160.00", BuchungPreiskalkulation.miete(booking));
        verify(discounts).findByMietbereichIdAndAktivTrue(3L);
    }

    @Test void changedTypeRecalculatesAbsoluteDiscountAndClearsPercentageSnapshot() {
        dto.setRabattwertTyp(RabattwertTyp.ABSOLUT);
        service.update(24L, dto);
        money("10.00", booking.getRabattbetrag());
        money("190.00", BuchungPreiskalkulation.miete(booking));
        assertNull(position.getRabattwert());
        assertNull(position.getRabattwertTyp());
        money("0", position.getRabattbetrag());
    }

    @Test void changedArtUsesCurrentCatalogRule() {
        booking.setRabattart(Rabattart.VEREIN);
        booking.setRabattwertTyp(null);
        booking.setRabattwert(null);
        var rule = new MietbereichRabatt();
        rule.setRabattwertTyp(RabattwertTyp.PROZENTUAL);
        rule.setRabattwert(new BigDecimal("25"));
        when(discounts.findByMietbereichIdAndRabattartAndAktivTrue(3L, Rabattart.VERBAND))
                .thenReturn(Optional.of(rule));
        dto.setRabattart(Rabattart.VERBAND);
        dto.setRabattwertTyp(null);
        dto.setRabattwert(null);
        service.update(24L, dto);
        money("25", position.getRabattwert());
        money("50.00", booking.getRabattbetrag());
        verify(discounts).findByMietbereichIdAndRabattartAndAktivTrue(3L, Rabattart.VERBAND);
    }

    @Test void absoluteToPercentageRecalculatesWithSameNumericValue() {
        booking.setRabattwertTyp(RabattwertTyp.ABSOLUT);
        position.setRabattwertTyp(null);
        position.setRabattwert(null);
        position.setRabattbetrag(BigDecimal.ZERO);
        when(discounts.findByMietbereichIdAndAktivTrue(3L)).thenReturn(List.of(new MietbereichRabatt()));
        service.update(24L, dto);
        assertEquals(RabattwertTyp.PROZENTUAL, position.getRabattwertTyp());
        money("10", position.getRabattwert());
        money("20.00", booking.getRabattbetrag());
        verify(discounts).findByMietbereichIdAndAktivTrue(3L);
    }

    @Test void bothNullValuesKeepVereinSnapshotDespiteChangedCatalog() {
        booking.setRabattart(Rabattart.VEREIN);
        booking.setRabattwertTyp(null);
        booking.setRabattwert(null);
        dto.setRabattart(Rabattart.VEREIN);
        dto.setRabattwertTyp(null);
        dto.setRabattwert(null);
        var changed = new MietbereichRabatt();
        changed.setRabattwertTyp(RabattwertTyp.PROZENTUAL);
        changed.setRabattwert(new BigDecimal("25"));
        when(discounts.findByMietbereichIdAndRabattartAndAktivTrue(3L, Rabattart.VEREIN))
                .thenReturn(Optional.of(changed));
        service.update(24L, dto);
        money("10.00", position.getRabattwert());
        money("20.00", booking.getRabattbetrag());
        verifyNoInteractions(discounts);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "10"})
    void nullToValueRecalculatesInsteadOfKeepingOldPositionSnapshot(String value) {
        booking.setRabattwert(null);
        dto.setRabattwert(new BigDecimal(value));
        when(discounts.findByMietbereichIdAndAktivTrue(3L)).thenReturn(List.of());
        service.update(24L, dto);
        assertNull(position.getRabattwert());
        money("0", booking.getRabattbetrag());
        verify(discounts).findByMietbereichIdAndAktivTrue(3L);
    }

    @Test void valueToNullStillRejectsIncompleteIndividualDiscount() {
        dto.setRabattwert(null);
        assertThrows(IllegalArgumentException.class, () -> service.update(24L, dto));
        verify(bookings, never()).flush();
    }

    @Test void removingDiscountClearsExistingSnapshot() {
        dto.setRabattart(Rabattart.KEIN_RABATT);
        dto.setRabattwertTyp(null);
        dto.setRabattwert(null);
        service.update(24L, dto);
        assertNull(position.getRabattwert());
        assertNull(position.getRabattwertTyp());
        money("0", booking.getRabattbetrag());
        money("200.00", BuchungPreiskalkulation.miete(booking));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"10", "10.0", "10.00"})
    void airbnbNeverRetainsDiscountEvenWhenSelectionIsUnchanged(String value) {
        booking.setBuchungsquelle(Buchungsquelle.AIRBNB);
        dto.setBuchungsquelle(Buchungsquelle.AIRBNB);
        booking.setRabattwert(value == null ? null : new BigDecimal(value));
        dto.setRabattwert(value == null ? null : new BigDecimal(value));
        service.update(24L, dto);
        assertEquals(Rabattart.KEIN_RABATT, booking.getRabattart());
        assertNull(booking.getRabattwertTyp());
        assertNull(booking.getRabattwert());
        assertNull(position.getRabattwert());
        assertNull(position.getRabattwertTyp());
        money("0", booking.getRabattbetrag());
        money("200.00", BuchungPreiskalkulation.miete(booking));
        verifyNoInteractions(discounts);
    }

    private static void money(String expected, BigDecimal actual) {
        assertNotNull(actual);
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
