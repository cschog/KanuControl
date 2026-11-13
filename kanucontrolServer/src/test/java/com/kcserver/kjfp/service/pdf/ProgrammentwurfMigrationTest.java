package com.kcserver.kjfp.service.pdf;

import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;
import java.sql.DriverManager;
import static org.assertj.core.api.Assertions.*;

class ProgrammentwurfMigrationTest {
    @Test void nullableColumnsAndSeparateTenants() throws Exception {
        try(var connection=DriverManager.getConnection("jdbc:h2:mem:programmentwurf;MODE=PostgreSQL;DB_CLOSE_DELAY=-1")) {
            var sql=connection.createStatement();
            for(String schema : new String[]{"tenant_a","tenant_b"}) {
                sql.execute("CREATE SCHEMA "+schema); connection.setSchema(schema.toUpperCase());
                sql.execute("CREATE TABLE veranstaltung(id BIGINT PRIMARY KEY)"); sql.execute("INSERT INTO veranstaltung(id) VALUES(42)");
                var db=DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection)); db.setDefaultSchemaName(schema.toUpperCase());
                var migration=new Liquibase("db/changelog/changes/20261113-04-programmentwurf.xml",new ClassLoaderResourceAccessor(),db);
                migration.update("");
                var rows=sql.executeQuery("SELECT programmentwurf_titel,programmentwurf_inhalt FROM veranstaltung WHERE id=42"); rows.next(); assertThat(rows.getString(1)).isNull(); assertThat(rows.getString(2)).isNull();
            }
            sql.execute("UPDATE tenant_a.veranstaltung SET programmentwurf_inhalt='<p>Nur A</p>' WHERE id=42");
            var rows=sql.executeQuery("SELECT programmentwurf_inhalt FROM tenant_b.veranstaltung WHERE id=42"); rows.next(); assertThat(rows.getString(1)).isNull();
        }
    }
}
