package com.kcserver.audit.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MaintenanceService {

    //private final AuditSessionService auditSessionService;

    private volatile boolean maintenanceMode = false;

    private volatile LocalDateTime maintenanceStart;

    private volatile String maintenanceMessage;

}
