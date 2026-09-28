package com.kcserver.dto.validation;

import com.kcserver.validation.ValidationMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationResultDTO {

    private boolean valid;
    private List<ValidationMessage> errors;
    private List<ValidationMessage> warnings;
}