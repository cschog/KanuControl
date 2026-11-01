package com.kcserver.vermietung.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class MietbereichVoraussetzungenDTO {
    private Set<Long> erforderlicheMietbereichIds = new HashSet<>();
}
