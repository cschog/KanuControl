package com.kcserver.audit.service;

import com.kcserver.audit.repository.AuditSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditSessionCleanupService {

    private final AuditSessionRepository repository;

    @Scheduled(fixedDelay = 300000) // alle 5 Minuten
    @Transactional
    public void closeInactiveSessions() {

        int count = repository.closeInactiveSessions(
                LocalDateTime.now().minusMinutes(35)
        );

        if (count > 0) {
            log.info("Closed {} inactive sessions", count);
        }
    }
}