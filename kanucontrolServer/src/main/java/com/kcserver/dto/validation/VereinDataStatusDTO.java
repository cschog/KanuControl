package com.kcserver.dto.validation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VereinDataStatusDTO {

    private DataStatus status = DataStatus.OK;

    private Map<String, DataFieldStatusDTO> fields = new HashMap<>();
}