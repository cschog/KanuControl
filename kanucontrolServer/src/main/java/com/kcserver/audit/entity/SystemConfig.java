package com.kcserver.audit.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class SystemConfig {

    @Id
    private Long id = 1L;

    private boolean maintenanceMode;

    private LocalDateTime maintenanceStart;

    private String maintenanceMessage;

}
