package com.kcserver.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class StartupLog {

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        System.out.println("🚀 KC Server läuft (ApplicationReady)");
    }
}
