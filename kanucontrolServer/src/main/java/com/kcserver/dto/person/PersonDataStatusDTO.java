package com.kcserver.dto.person;

import com.kcserver.dto.validation.DataFieldStatusDTO;
import com.kcserver.dto.validation.DataStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonDataStatusDTO {

    private DataStatus status = DataStatus.OK;

    private Map<String, DataFieldStatusDTO> fields = new HashMap<>();
}