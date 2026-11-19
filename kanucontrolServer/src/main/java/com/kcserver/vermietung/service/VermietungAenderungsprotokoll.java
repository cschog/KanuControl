package com.kcserver.vermietung.service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor
public class VermietungAenderungsprotokoll {
 private final com.kcserver.vermietung.repository.VermietungAenderungRepository repository;
 private final com.fasterxml.jackson.databind.ObjectMapper mapper;
 @Transactional public void schreiben(String aktion,Object daten) {
  VorlagenService.tenant();var auth=org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
  if(auth==null || !auth.isAuthenticated())throw new org.springframework.security.access.AccessDeniedException("Anmeldung erforderlich.");
  var e=new com.kcserver.vermietung.entity.VermietungAenderung();e.setZeitpunkt(java.time.Instant.now());e.setBenutzer(auth.getName());e.setAktion(aktion);
  try{e.setDaten(mapper.writeValueAsString(daten));}catch(com.fasterxml.jackson.core.JsonProcessingException ex){throw new IllegalStateException(ex);}
  repository.save(e);
 }
}
