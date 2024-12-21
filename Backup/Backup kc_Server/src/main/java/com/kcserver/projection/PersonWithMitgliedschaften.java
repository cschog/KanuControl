package com.kcserver.projection;

import com.kcserver.entity.Mitglied;
import com.kcserver.entity.Person;
import com.kcserver.entity.Verein;
import org.springframework.data.rest.core.config.Projection;

import java.util.List;

@Projection(name = "personWithMitgliedschaften", types = {Person.class})
public interface PersonWithMitgliedschaften {
    String getName();
    String getVorname();
    String getStrasse();
    String getPlz();
    String getOrt();
    String getTelefon();
    String getBankName();
    String getIban();
    String getBic();

    List<MitgliedProjection> getMitgliedschaften();
}

@Projection(name = "mitgliedProjection", types = {Mitglied.class})
interface MitgliedProjection {
    Long getId();
    String getFunktion();
    Boolean getHauptVerein();
    VereinProjection getVereinMitgliedschaft();
}

@Projection(name = "vereinProjection", types = {Verein.class})
interface VereinProjection {
    Long getId();
    String getName();
    String getAbk();
}