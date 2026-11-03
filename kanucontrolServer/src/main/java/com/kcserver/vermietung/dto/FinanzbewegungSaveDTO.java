package com.kcserver.vermietung.dto;
import com.kcserver.vermietung.enumtype.Finanzbewegungsart;
import com.kcserver.vermietung.enumtype.Finanzkategorie;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
public record FinanzbewegungSaveDTO(@NotNull Finanzkategorie kategorie, @NotNull Finanzbewegungsart art, @NotNull @DecimalMin("0.01") BigDecimal betrag, @NotNull LocalDate datum, String bemerkung) {}
