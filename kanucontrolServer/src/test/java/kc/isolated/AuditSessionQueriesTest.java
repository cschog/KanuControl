package kc.isolated;

import com.kcserver.core.audit.entity.AuditSession;
import com.kcserver.core.audit.repository.AuditSessionRepository;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/** Prüft die echten Repository-Abfragen einschließlich Pagination und Zählern. */
class AuditSessionQueriesTest {
    private SessionFactory factory;
    private EntityManager manager;
    private AuditSessionRepository repository;
    private final LocalDateTime now = LocalDateTime.of(2026, 11, 4, 12, 0);

    @BeforeEach void setup() {
        factory = new Configuration().addAnnotatedClass(AuditSession.class)
                .setProperty("hibernate.connection.url", "jdbc:h2:mem:" + UUID.randomUUID())
                .setProperty("hibernate.hbm2ddl.auto", "create-drop")
                .setProperty("hibernate.hbm2ddl.create_namespaces", "true").buildSessionFactory();
        manager = factory.createEntityManager();
        repository = new JpaRepositoryFactory(manager).getRepository(AuditSessionRepository.class);
        manager.getTransaction().begin();
        add("alice", "club_a", false, false, now.minusMinutes(10));
        add("admin", "club_b", false, true, now.minusMinutes(9));
        add("bob", "club_a", false, false, now.minusMinutes(8));
        add("done", "club_a", true, true, now.minusMinutes(7));
        add("old", "club_a", true, true, now.minusDays(1));
        add("service-account-ekc-homepage", "technical_only", false, true, now.minusMinutes(1));
        add("SERVICE-ACCOUNT-OTHER", "club_a", false, true, now.minusMinutes(2));
        add("service-account-closed", "club_a", true, true, now.minusMinutes(3));
        manager.getTransaction().commit();
    }
    private void add(String username, String tenant, boolean closed, boolean external, LocalDateTime login) {
        var session = new AuditSession();
        session.setUsername(username); session.setTenant(tenant); session.setSessionId(UUID.randomUUID().toString());
        session.setLoginTime(login); session.setLastSeen(now); session.setLogoutTime(closed ? now : null);
        session.setIpAddress(external ? "203.0.113.1" : "192.168.100.10");
        manager.persist(session);
    }
    @AfterEach void close() { manager.close(); factory.close(); }

    @Test void aktiveSitzungenEnthaltenNurMenschen() {
        assertEquals(Set.of("alice", "admin", "bob"),
                new HashSet<>(repository.findActiveSessions().stream().map(AuditSession::getUsername).toList()));
    }
    @Test void historieFiltertVorPaginationUndZaehltNurPersoenlicheSitzungen() {
        var first = repository.findHistory(PageRequest.of(0, 2));
        assertEquals(5, first.getTotalElements());
        assertEquals(List.of("done", "bob"), first.getContent().stream().map(AuditSession::getUsername).toList());
        var last = repository.findHistory(PageRequest.of(2, 2));
        assertEquals(List.of("old"), last.getContent().stream().map(AuditSession::getUsername).toList());
    }
    @Test void bannerZeigtNurAnderePersoenlicheBenutzerDesMandanten() {
        assertEquals(List.of("bob"), repository.findOtherOnlineUsers("club_a", "alice", now.minusMinutes(5)));
    }
    @Test void dashboardZaehltKeineTechnischenSitzungenOderDerenMandanten() {
        assertEquals(3, repository.countByLogoutTimeIsNull());
        assertEquals(4, repository.countByLoginTimeAfter(now.toLocalDate().atStartOfDay()));
        assertEquals(1, repository.countExternalSessions());
        assertEquals(2, repository.countActiveTenants());
    }
}
