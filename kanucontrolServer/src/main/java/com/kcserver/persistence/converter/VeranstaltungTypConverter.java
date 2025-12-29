package com.kcserver.persistence.converter;

import com.kcserver.enumtype.VeranstaltungTyp;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class VeranstaltungTypConverter
        extends AbstractCodeEnumConverter<VeranstaltungTyp> {

    public VeranstaltungTypConverter() {
        super(VeranstaltungTyp.class);
    }
}