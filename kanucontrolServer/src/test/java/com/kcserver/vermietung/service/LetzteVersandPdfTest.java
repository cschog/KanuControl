package com.kcserver.vermietung.service;

import com.kcserver.vermietung.entity.VorlagenVersand;
import com.kcserver.vermietung.repository.VorlagenVersandRepository;
import org.junit.jupiter.api.Test;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypes;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class LetzteVersandPdfTest {
 @Test void ersetzungEntferntNurFrueherePdfUndBewahrtVersandkennung(){
  var factory=new LocalContainerEntityManagerFactoryBean();
  factory.setDataSource(new DriverManagerDataSource("jdbc:h2:mem:lastpdf;DB_CLOSE_DELAY=-1","sa",""));
  factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
  factory.setManagedTypes(PersistenceManagedTypes.of(VorlagenVersand.class.getName()));
  factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto","create-drop"));factory.afterPropertiesSet();
  try(var em=factory.getObject().createEntityManager()){
   var repo=new JpaRepositoryFactory(em).getRepository(VorlagenVersandRepository.class);
   var old=eintrag(1L,"RESERVIERUNG","old");var current=eintrag(1L,"RESERVIERUNG","new");
   var otherBooking=eintrag(2L,"RESERVIERUNG","other");var otherKind=eintrag(1L,"ABGERECHNET","invoice");
   em.getTransaction().begin();em.persist(old);em.persist(current);em.persist(otherBooking);em.persist(otherKind);em.flush();
   repo.entferneFrueherePdfs(1L,"RESERVIERUNG",current.getId(),new byte[0]);em.clear();
   assertEquals(0,repo.findByVersandSchluessel("old").orElseThrow().getPdf().length);
   var metadata=repo.findByVersandSchluessel("old").orElseThrow().getMetadaten();assertFalse(metadata.contains("AQI="));assertTrue(metadata.contains("keep"));
   assertArrayEquals(new byte[]{1,2},repo.findByVersandSchluessel("new").orElseThrow().getPdf());
   assertEquals(2,repo.findByVersandSchluessel("other").orElseThrow().getPdf().length);
   assertEquals(2,repo.findByVersandSchluessel("invoice").orElseThrow().getPdf().length);
   repo.entferneFrueherePdfs(1L,"RESERVIERUNG",null,new byte[0]);em.clear();
   assertEquals(0,repo.findByVersandSchluessel("new").orElseThrow().getPdf().length);
   em.getTransaction().commit();
  }finally{factory.destroy();}
 }
 private VorlagenVersand eintrag(Long buchung,String art,String key){
  var e=new VorlagenVersand();e.setBuchungId(buchung);e.setMietobjektId(7L);e.setVorgang(art);
  e.setVersandSchluessel(key);e.setPdf(new byte[]{1,2});e.setMetadaten("{\"pdf\":\"AQI=\",\"auftragHash\":\"keep\"}");return e;
 }
}
