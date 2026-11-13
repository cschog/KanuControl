package com.kcserver.vermietung.service;

import com.kcserver.vermietung.config.SmtpMailProperties;
import com.kcserver.vermietung.entity.HomepageAnbindung;
import com.kcserver.vermietung.repository.*;
import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BuchungsdokumentMailTest {
 @Test void manuelleMailNutztBestehendesTestRoutingUndGlobaleSperreAberNichtAutomatikSchalter()throws Exception{
  try(var factory=Validation.buildDefaultValidatorFactory()){
   var configs=mock(HomepageAnbindungRepository.class);var sender=mock(JavaMailSender.class);
   @SuppressWarnings("unchecked") ObjectProvider<JavaMailSender> provider=mock(ObjectProvider.class);
   when(provider.getIfAvailable()).thenReturn(sender);when(sender.createMimeMessage()).thenAnswer(i->new MimeMessage(Session.getInstance(new Properties())));
   var smtp=new SmtpMailProperties();smtp.setEnabled(true);smtp.setFrom("club@example.org");
   var service=new HomepageStatusMailService(mock(HomepageStatusMailRepository.class),configs,factory.getValidator(),provider,smtp);
   var b=ReservierungsbestaetigungTest.beispiel();b.getMieter().setEmail("mieter@example.org");
   var config=new HomepageAnbindung();config.setGastBestaetigung(false);config.setAntwortCcEmail("cc@example.org");config.setAntwortTestEmail("object-test@example.org");when(configs.findById(7L)).thenReturn(Optional.of(config));
   var route=service.nachrichtVorschau(b,"Betreff","Text");assertEquals("object-test@example.org",route.empfaenger());assertNull(route.cc());
   smtp.setTestMode(true);smtp.setTestRecipient("global-test@example.org");route=service.nachrichtVorschau(b,"Betreff","Text");assertEquals("global-test@example.org",route.empfaenger());assertNull(route.cc());
   byte[] pdf=java.nio.file.Files.readAllBytes(java.nio.file.Path.of("src/main/resources/pdf/Reservierungsbestaetigung.pdf"));
   var mail=service.nachrichtVorbereiten(route,pdf,"Extern.pdf","metadata");mail.message().saveChanges();assertEquals("global-test@example.org",mail.message().getRecipients(Message.RecipientType.TO)[0].toString());assertNull(mail.message().getRecipients(Message.RecipientType.CC));assertNull(mail.message().getRecipients(Message.RecipientType.BCC));
   var parts=new ArrayList<Part>();collect(mail.message(),parts);var attachment=parts.stream().filter(p->{try{return p.getFileName()!=null;}catch(Exception ex){throw new RuntimeException(ex);}}).findFirst().orElseThrow();assertArrayEquals(pdf,attachment.getInputStream().readAllBytes());
   smtp.setEnabled(false);var finalRoute=route;assertThrows(Exception.class,()->service.nachrichtVorbereiten(finalRoute,pdf,"Extern.pdf","meta"));verify(sender,never()).send(any(MimeMessage.class));
   smtp.setTestMode(false);config.setAntwortTestEmail(null);route=service.nachrichtVorschau(b,"Betreff","Text");assertEquals("mieter@example.org",route.empfaenger());assertEquals("cc@example.org",route.cc());
   b.getMieter().setEmail("ungültig");assertThrows(Exception.class,()->service.nachrichtVorschau(b,"Betreff","Text"));
  }
 }
 private static void collect(Part part,List<Part> result)throws Exception{if(part.isMimeType("multipart/*")){var multi=(Multipart)part.getContent();for(int i=0;i<multi.getCount();i++)collect(multi.getBodyPart(i),result);}else result.add(part);}
}
