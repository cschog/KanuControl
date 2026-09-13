package com.kcserver.dto.finanzen;

import com.kcserver.enumtype.FinanzausgleichGesamtstatus;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FinanzausgleichPruefungDTO {

    private BigDecimal gesamtSoll;

    private BigDecimal gesamtUeberweisungen;

    private BigDecimal gesamtQuittungen;

    private BigDecimal gesamtIst;

    private FinanzausgleichGesamtstatus status;

    private List<FinanzausgleichDTO> finanzgruppen;
}