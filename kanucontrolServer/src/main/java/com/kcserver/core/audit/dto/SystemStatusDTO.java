package com.kcserver.core.audit.dto;

import lombok.*;

import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemStatusDTO {

    private boolean maintenanceMode;

    private LocalDateTime maintenanceStart;

    private String maintenanceMessage;

    private int activeUsers;

}
