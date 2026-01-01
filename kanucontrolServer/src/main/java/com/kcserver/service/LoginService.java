package com.kcserver.service;

import com.kcserver.repository.PersonRepository;
import com.kcserver.tenancy.TenantContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class LoginService {

    private final PersonRepository personRepository;

    public LoginService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public void login(String tenant) {

        TenantContext.setCurrentTenant(tenant);
        try {
            // ganz normal JPA verwenden
            personRepository.findAll();
        } finally {
            TenantContext.clear();
        }
    }
}