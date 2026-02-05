package com.kcserver.dto.teilnehmer;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TeilnehmerBulkDeleteDTO {

    @NotEmpty
    private List<Long> personIds;
}