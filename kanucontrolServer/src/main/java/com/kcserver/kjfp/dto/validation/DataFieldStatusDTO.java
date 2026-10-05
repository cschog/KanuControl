package com.kcserver.kjfp.dto.validation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataFieldStatusDTO {

    private DataStatus status;

    private String message;
}