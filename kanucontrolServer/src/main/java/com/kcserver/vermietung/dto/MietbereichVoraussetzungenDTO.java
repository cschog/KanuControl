package com.kcserver.vermietung.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class MietbereichVoraussetzungenDTO {
    // Legacy field: clients predating source-specific requirements configured direct bookings.
    private Set<Long> erforderlicheMietbereichIds = new HashSet<>();
    private Set<Long> erforderlicheMietbereichIdsDirekt;
    private Set<Long> erforderlicheMietbereichIdsAirbnb;
    private Set<Long> belegteMietbereichIds;
}
