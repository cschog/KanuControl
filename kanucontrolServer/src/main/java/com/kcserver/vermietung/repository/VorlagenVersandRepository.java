package com.kcserver.vermietung.repository;
import com.kcserver.vermietung.entity.VorlagenVersand;
public interface VorlagenVersandRepository extends org.springframework.data.jpa.repository.JpaRepository<VorlagenVersand,String> {
    java.util.Optional<VorlagenVersand> findByVersandSchluessel(String schluessel);
    java.util.List<VorlagenVersand> findByBuchungIdOrderByErstelltAmDesc(Long buchung);
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("delete from VorlagenVersand e where e.buchungId = :id")
    void deleteAllByBuchungId(@org.springframework.data.repository.query.Param("id") Long id);
    default void entferneFrueherePdfs(Long buchung,String vorgang,String aktuell,byte[] leer){
        for(var e:findByBuchungIdOrderByErstelltAmDesc(buchung)){
            if(!e.getVorgang().equals(vorgang) || e.getId().equals(aktuell))continue;
            e.setPdf(leer.clone());
            // Legacy message metadata also contained a base64 copy of the attachment.
            try{
                var mapper=new com.fasterxml.jackson.databind.ObjectMapper();
                var json=mapper.readTree(e.getMetadaten());
                if(json instanceof com.fasterxml.jackson.databind.node.ObjectNode object){
                    object.remove("pdf");object.remove("originalPdf");e.setMetadaten(mapper.writeValueAsString(object));
                }
            }catch(com.fasterxml.jackson.core.JsonProcessingException ignored){/* Older metadata may be plain text. */}
            saveAndFlush(e);
        }
    }
    long countByBuchungIdAndVorgang(Long id, String vorgang);
}
