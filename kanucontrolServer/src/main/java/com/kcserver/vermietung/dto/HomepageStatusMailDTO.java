package com.kcserver.vermietung.dto;

import java.time.Instant;

/** Nur an den authentifizierten Adapter; niemals öffentlich an Formularbesucher liefern. */
public record HomepageStatusMailDTO(String id, String buchungsnummer, String status,
        String empfaenger, String replyTo, String betreff, String text, Instant createdAt) {}
