package com.kcserver.unit;

import com.kcserver.vermietung.dto.BuchungDTO;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BuchungVeranstaltungTest {
    @Test void backendAcceptsMissingEmptyAndEightyCharactersButRejectsEightyOne() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var dto = new BuchungDTO();
            assertTrue(validator.validateProperty(dto, "veranstaltung").isEmpty());
            dto.setVeranstaltung("");
            assertTrue(validator.validateProperty(dto, "veranstaltung").isEmpty());
            dto.setVeranstaltung("V".repeat(80));
            assertTrue(validator.validateProperty(dto, "veranstaltung").isEmpty());
            dto.setVeranstaltung("V".repeat(81));
            assertFalse(validator.validateProperty(dto, "veranstaltung").isEmpty());
        }
    }
    @Test void migrationPreservesExistingTextAndAllowsEightyCharacters() throws Exception {
        try (var connection = java.sql.DriverManager.getConnection("jdbc:h2:mem:" + java.util.UUID.randomUUID() + ";MODE=PostgreSQL")) {
            connection.createStatement().execute("CREATE TABLE buchung (id BIGINT PRIMARY KEY, veranstaltung VARCHAR(30))");
            connection.createStatement().execute("INSERT INTO buchung VALUES (1, 'Jugendveranstaltung')");
            var database = liquibase.database.DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new liquibase.database.jvm.JdbcConnection(connection));
            var migration = new liquibase.Liquibase("db/changelog/changes/20261113-02-buchung-veranstaltung-80.xml", new liquibase.resource.ClassLoaderResourceAccessor(), database);
            migration.update(new liquibase.Contexts(), new liquibase.LabelExpression());
            try (var row = connection.createStatement().executeQuery("SELECT veranstaltung FROM buchung WHERE id=1")) {
                assertTrue(row.next());assertEquals("Jugendveranstaltung", row.getString(1));
            }
            try (var insert = connection.prepareStatement("INSERT INTO buchung VALUES (?, ?)")) {
                insert.setInt(1, 2);insert.setString(2, "V".repeat(80));assertEquals(1, insert.executeUpdate());
                insert.setInt(1, 3);insert.setString(2, "V".repeat(81));assertThrows(java.sql.SQLException.class, insert::executeUpdate);
            }
        }
    }

}
