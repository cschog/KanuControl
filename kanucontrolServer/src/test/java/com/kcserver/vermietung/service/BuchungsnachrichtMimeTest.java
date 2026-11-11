package com.kcserver.vermietung.service;
import com.kcserver.vermietung.repository.*;
import com.kcserver.vermietung.config.SmtpMailProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class BuchungsnachrichtMimeTest {
 @Test void individuelleMailUndExternesPdfWerdenAlsExakteMimeNachrichtVorbereitet()throws Exception{
  ObjectProvider<JavaMailSender> provider=mock(ObjectProvider.class);var sender=new JavaMailSenderImpl();when(provider.getIfAvailable()).thenReturn(sender);
  var smtp=new SmtpMailProperties();smtp.setEnabled(true);
  var service=new HomepageStatusMailService(mock(HomepageStatusMailRepository.class),mock(HomepageAnbindungRepository.class),mock(jakarta.validation.Validator.class),provider,smtp);
  byte[] bytes={37,80,68,70,45,49,46,52,10,1,2,3};
  var route=new HomepageStatusMailService.StatusVorschau("club@example.org","mieter@example.org","copy@example.org","reply@example.org","Individueller Betreff","Individueller Mailtext",false,null);
  var prepared=service.nachrichtVorbereiten(route,bytes,"Bearbeitet.pdf","Metadaten");var msg=prepared.message();msg.saveChanges();assertEquals("Individueller Betreff",msg.getSubject());assertEquals("reply@example.org",msg.getReplyTo()[0].toString());
  var parts=(jakarta.mail.Multipart)msg.getContent();assertEquals(2,parts.getCount());assertArrayEquals(bytes,parts.getBodyPart(1).getInputStream().readAllBytes());assertEquals("Bearbeitet.pdf",parts.getBodyPart(1).getFileName());assertArrayEquals(bytes,prepared.pdf());
 }
}
