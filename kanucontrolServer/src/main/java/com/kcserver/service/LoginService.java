package com.kcserver.service;

import com.kcserver.audit.repository.AuditSessionRepository;
import com.kcserver.repository.PersonRepository;
import com.kcserver.tenancy.TenantContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class LoginService {

    private final PersonRepository personRepository;

    private final AuditSessionRepository auditRepo;

    public void login(String tenant) {

        try {

            TenantContext.setCurrentTenant(tenant);

            personRepository.count();

        } finally {

            TenantContext.clear();
        }
    }
}