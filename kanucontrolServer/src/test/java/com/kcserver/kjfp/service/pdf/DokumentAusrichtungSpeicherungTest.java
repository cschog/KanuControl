package com.kcserver.kjfp.service.pdf;

import com.kcserver.kjfp.entity.Dokument;
import com.kcserver.kjfp.entity.abrechnung.AbrechnungBeleg;
import com.kcserver.kjfp.mapper.DokumentMapper;
import com.kcserver.kjfp.repository.abrechnung.*;
import com.kcserver.kjfp.service.abrechnung.DokumentService;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DokumentAusrichtungSpeicherungTest {
    @Test void savesOnlyOwnedDocumentAndDoesNotMutateOriginalOrUnsubmittedDraft()throws Exception {
        var documents=mock(DokumentRepository.class);var belege=mock(AbrechnungBelegRepository.class);
        var ausrichtung=new DokumentAusrichtung();
        var service=new DokumentService(documents,ausrichtung,belege,null,new DokumentMapper(),null);
        var owner=new AbrechnungBeleg();owner.setId(4L);var wrongOwner=new AbrechnungBeleg();wrongOwner.setId(5L);
        var doc=DokumentAusrichtungTest.dokument(DokumentAusrichtungTest.pdf(0,90),"application/pdf");doc.setBeleg(owner);
        byte[] original=doc.getInhalt().clone();
        when(belege.findById(4L)).thenReturn(Optional.of(owner));when(belege.findById(5L)).thenReturn(Optional.of(wrongOwner));
        when(documents.findById(9L)).thenReturn(Optional.of(doc));
        assertThatThrownBy(()->service.updateAusrichtung(5L,9L,new DokumentAusrichtung.Aenderung(List.of(180,0),0L))).hasMessageContaining("404");
        assertThat(doc.getSeitenDrehungen()).isNull();verify(documents,never()).flush();
        // Ein lediglich angezeigter Entwurf speichert nichts.
        ausrichtung.vorschau(doc,0);assertThat(doc.getSeitenDrehungen()).isNull();
        doAnswer(i->{doc.setAusrichtungVersion(doc.getAusrichtungVersion()+1);return null;}).when(documents).flush();
        var result=service.updateAusrichtung(4L,9L,new DokumentAusrichtung.Aenderung(List.of(180,90),0L));
        assertThat(result.drehungen()).containsExactly(180,90);assertThat(result.version()).isEqualTo(1);
        assertThat(doc.getInhalt()).isEqualTo(original);
        assertThatThrownBy(()->service.updateAusrichtung(4L,9L,new DokumentAusrichtung.Aenderung(List.of(0,0),0L))).hasMessageContaining("409");
        assertThat(ausrichtung.info(doc).drehungen()).containsExactly(180,90);
    }
    @Test void tenantMigrationKeepsOriginalAndDefaultsAndIsolatesSchemas()throws Exception {
        try(var connection=java.sql.DriverManager.getConnection("jdbc:h2:mem:rotation_"+UUID.randomUUID()+";MODE=PostgreSQL")) {
            for(String schema:List.of("a","b")) {
                var sql=connection.createStatement();sql.execute("CREATE SCHEMA "+schema);connection.setSchema(schema.toUpperCase());
                sql.execute("CREATE TABLE dokument(id BIGINT PRIMARY KEY, inhalt BYTEA)");sql.execute("INSERT INTO dokument VALUES (9,X'010203')");
                var db=liquibase.database.DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new liquibase.database.jvm.JdbcConnection(connection));db.setDefaultSchemaName(schema.toUpperCase());
                var lb=new liquibase.Liquibase("db/changelog/changes/20261116-01-dokument-ausrichtung.xml",new liquibase.resource.ClassLoaderResourceAccessor(),db);lb.update(new liquibase.Contexts(),new liquibase.LabelExpression());
                var row=sql.executeQuery("SELECT inhalt,seiten_drehungen,ausrichtung_version FROM dokument WHERE id=9");row.next();assertThat(row.getBytes(1)).containsExactly(1,2,3);assertThat(row.getString(2)).isNull();assertThat(row.getLong(3)).isZero();
            }
            connection.createStatement().execute("UPDATE a.dokument SET seiten_drehungen='[180]' WHERE id=9");
            var row=connection.createStatement().executeQuery("SELECT seiten_drehungen FROM b.dokument WHERE id=9");row.next();assertThat(row.getString(1)).isNull();
        }
    }
}
