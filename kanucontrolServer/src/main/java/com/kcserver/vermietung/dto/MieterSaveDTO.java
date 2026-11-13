package com.kcserver.vermietung.dto;

import com.kcserver.core.validation.ValidIban;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MieterSaveDTO {
    private Long personId;
    @NotBlank(message="Bitte einen Nachnamen eingeben.")
    @Size(min=2,max=100,message="Der Nachname muss zwischen 2 und 100 Zeichen lang sein.") private String name;
    @NotBlank(message="Bitte einen Vornamen eingeben.")
    @Size(min=2,max=100,message="Der Vorname muss zwischen 2 und 100 Zeichen lang sein.") private String vorname;
    @NotBlank(message="Bitte eine E-Mail-Adresse eingeben.")
    @Email(message="Bitte eine gültige E-Mail-Adresse eingeben.")
    @Size(max=255,message="Die E-Mail-Adresse darf höchstens 255 Zeichen enthalten.") private String email;
    private String telefon;
    private String telefonFestnetz;
    private String strasse;
    private String plz;
    private String ort;
    private String countryCode = "DE";
    @jakarta.validation.constraints.Size(max=255)
    private String organisation;
    @jakarta.validation.constraints.Size(max=255)
    private String kontoinhaber;
    private String bankName;
    @ValidIban(message="Die IBAN ist ungültig. Bitte Länderkennung, Länge und Prüfziffer prüfen oder das Feld leer lassen.") private String iban;
    private String bic;
    private Boolean aktiv;
    // Vor der Bean-Validierung normalisieren, nicht erst beim Speichern der Person.
    public void setName(String value) { name=trim(value); }
    public void setVorname(String value) { vorname=trim(value); }
    public void setEmail(String value) { email=trim(value); }
    public void setIban(String value) { iban=value==null?null:value.replaceAll("\\s+", "").toUpperCase(java.util.Locale.ROOT); }
    private static String trim(String value) { return value==null?null:value.strip(); }

}
