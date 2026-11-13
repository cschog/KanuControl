package com.kcserver.unit;

import com.kcserver.core.util.PersonName;
import com.kcserver.vermietung.service.VorlagenModell;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class PersonNameTest {
    @Test void underscoreOnlyFirstNamesAreHiddenInBothOrders() {
        for (String first : new String[]{"_", "_____", "  _____  ", "", " "}) {
            assertEquals("Kanuverein", PersonName.surnameFirst("Kanuverein", first));
            assertEquals("Kanuverein", PersonName.givenFirst("Kanuverein", first));
        }
        assertEquals("Kanuverein", PersonName.surnameFirst("Kanuverein", null));
    }
    @Test void regularNamesAndNamesContainingUnderscoresRemainVisible() {
        assertEquals("Muster, Max", PersonName.surnameFirst("Muster", "Max"));
        assertEquals("Max Muster", PersonName.givenFirst("Muster", "Max"));
        assertEquals("Muster, Max_", PersonName.surnameFirst("Muster", "Max_"));
        assertEquals("Max", PersonName.surnameFirst(null, "Max"));
        assertEquals("", PersonName.surnameFirst(null, null));
    }
    @Test void documentTemplatesOmitOnlyTheSeparatorForPlaceholderFirstNames() {
        var data = Map.of("nachname", "Kanuverein", "vorname", "_____");
        assertEquals("Kanuverein", VorlagenModell.ersetzen("{{nachname}}, {{vorname}}", data));
        assertEquals("Kanuverein", VorlagenModell.ersetzen("{{vorname}} {{nachname}}", data));
        assertEquals("Hallo Kanuverein,", VorlagenModell.ersetzen("Hallo {{vorname}} {{nachname}},", data));
        assertEquals("Hallo Kanuverein,", VorlagenModell.ersetzen("Hallo {{vorname}},", data));
        assertEquals("_____", data.get("vorname"));
        assertEquals("Muster, Max", VorlagenModell.ersetzen("{{nachname}}, {{vorname}}", Map.of("nachname", "Muster", "vorname", "Max")));
    }
}
