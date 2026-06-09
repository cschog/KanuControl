package com.kcserver.audit.controller;

import com.kcserver.audit.service.AuditSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/session")
public class SessionController {

    private final AuditSessionService auditSessionService;

    @GetMapping("/online-users")
    public List<String> getOnlineUsers() {
        return auditSessionService.getOnlineUsers();
    }
}
