package com.kcserver.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.vermietung.dto.MietobjektDTO;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.BankverbindungAuswahl;
import com.kcserver.vermietung.repository.MietobjektRepository;
import com.kcserver.vermietung.service.*;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BankverbindungTest {
    private Bankverbindung primaer() { return new Bankverbindung(" Verein A ", " Bank A ", "de89 3704 0044 0532 0130 00", "cobadeffxxx"); }
    private Bankverbindung alternativ() { return new Bankverbindung("Verein B", "Bank B", "DE12500105170648489890", "INGDDEFFXXX"); }

    @Test void beideKontenWerdenMitAllenVierFeldernGespeichertUndGeladen() throws Exception {
        var repository = mock(MietobjektRepository.class);
        when(repository.save(any())).thenAnswer(call -> { Mietobjekt o = call.getArgument(0); o.setId(7L); return o; });
        var service = new MietobjektService(repository);
        var dto = new MietobjektDTO(); dto.setBezeichnung("Haus");
        dto.setBankverbindungPrimaer(primaer()); dto.setBankverbindungAlternativ(alternativ());
        var created = service.create(dto);
        assertBank(created.getBankverbindungPrimaer(), "Verein A", "Bank A", "DE89370400440532013000", "COBADEFFXXX");
        assertBank(created.getBankverbindungAlternativ(), "Verein B", "Bank B", "DE12500105170648489890", "INGDDEFFXXX");
        var captor = org.mockito.ArgumentCaptor.forClass(Mietobjekt.class);
        verify(repository).save(captor.capture());
        when(repository.findById(7L)).thenReturn(Optional.of(captor.getValue()));
        dto.getBankverbindungAlternativ().setKontoinhaber("Neuer Inhaber");
        dto.getBankverbindungAlternativ().setBankName("Neue Bank");
        service.update(7L, dto);
        assertBank(service.getById(7L).getBankverbindungAlternativ(), "Neuer Inhaber", "Neue Bank", "DE12500105170648489890", "INGDDEFFXXX");
        var mapper = new ObjectMapper().findAndRegisterModules();
        String json = mapper.writeValueAsString(service.getById(7L));
        assertFalse(json.contains("vollstaendig"));
        assertEquals("Neuer Inhaber", mapper.readValue(json, MietobjektDTO.class).getBankverbindungAlternativ().getKontoinhaber());
    }

    @Test void validierungPrueftBeideIbansUndFehlendeIban() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var dto = new MietobjektDTO(); dto.setBezeichnung("Haus");
            dto.setBankverbindungPrimaer(primaer()); dto.setBankverbindungAlternativ(alternativ());
            assertTrue(validator.validate(dto).isEmpty());
            dto.getBankverbindungAlternativ().setIban("DE00000000000000000000");
            assertFalse(validator.validate(dto).isEmpty());
            dto.setBankverbindungAlternativ(alternativ()); dto.getBankverbindungPrimaer().setIban("");
            assertFalse(validator.validate(dto).isEmpty());
            dto.setBankverbindungPrimaer(null); dto.setBankverbindungAlternativ(null);
            assertTrue(validator.validate(dto).isEmpty());
        }
        assertNull(Bankverbindung.normalisiert(new Bankverbindung(" ", "", null, "")));
    }

    @Test void standardAuswahlAlternativUndAeltereClients() {
        var objekt = new Mietobjekt(); objekt.setBankverbindungAlternativ(alternativ());
        var buchung = new Buchung();
        BuchungBankverbindung.waehlen(buchung, objekt, null);
        assertEquals(BankverbindungAuswahl.PRIMAER, buchung.getBankverbindungAuswahl());
        BuchungBankverbindung.waehlen(buchung, objekt, BankverbindungAuswahl.ALTERNATIV);
        BuchungBankverbindung.waehlen(buchung, objekt, null);
        assertEquals(BankverbindungAuswahl.ALTERNATIV, buchung.getBankverbindungAuswahl());
        BuchungBankverbindung.waehlen(buchung, objekt, BankverbindungAuswahl.PRIMAER);
        assertEquals(BankverbindungAuswahl.PRIMAER, buchung.getBankverbindungAuswahl());
    }

    @Test void fehlendesAlternativkontoUndFremdesObjektWerdenAbgewiesen() {
        var objekt = new Mietobjekt(); var buchung = new Buchung();
        assertThrows(IllegalArgumentException.class, () -> BuchungBankverbindung.waehlen(buchung, objekt, BankverbindungAuswahl.ALTERNATIV));
        objekt.setBankverbindungAlternativ(new Bankverbindung("Name", "Bank", " ", ""));
        assertThrows(IllegalArgumentException.class, () -> BuchungBankverbindung.waehlen(buchung, objekt, BankverbindungAuswahl.ALTERNATIV));
        assertEquals(BankverbindungAuswahl.PRIMAER, buchung.getBankverbindungAuswahl());
    }

    private void assertBank(Bankverbindung b, String inhaber, String name, String iban, String bic) {
        assertEquals(inhaber,b.getKontoinhaber()); assertEquals(name,b.getBankName());
        assertEquals(iban,b.getIban()); assertEquals(bic,b.getBic());
    }
}
