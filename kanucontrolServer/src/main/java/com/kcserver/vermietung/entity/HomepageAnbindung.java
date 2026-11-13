package com.kcserver.vermietung.entity;

import com.kcserver.core.audit.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;

/** Einstellungen im Schema des aktuellen Mandanten; keine Zugangsdaten. */
@Entity
@Table(name = "homepage_anbindung")
@Getter
@Setter
public class HomepageAnbindung extends Auditable {
    @Id
    @Column(name = "mietobjekt_id")
    private Long mietobjektId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mietobjekt_id")
    private Mietobjekt mietobjekt;

    @Column(nullable = false)
    private boolean aktiv;

    @Column(name = "technischer_client_id", length = 200)
    private String technischerClientId;

    @Version
    private Long version;

    @Column(nullable = false, length = 100)
    private String bezeichnung = "Homepage";

    @Column(name = "homepage_url", length = 2048)
    private String homepageUrl;

    @Column(name = "datenschutz_url", length = 2048)
    private String datenschutzUrl;

    @Column(name = "hausregeln_url", length = 2048)
    private String hausregelnUrl;

    @Column(name = "empfaenger_email", length = 254)
    private String empfaengerEmail;

    @Column(name = "gast_bestaetigung", nullable = false)
    private boolean gastBestaetigung = true;

    @Column(name = "verwaltungs_benachrichtigung", nullable = false)
    private boolean verwaltungsBenachrichtigung;

    @Column(name = "teilnehmer_pflicht", nullable = false)
    private boolean teilnehmerPflicht = true;

    @Column(name = "hausmeister_email", length = 254)
    private String hausmeisterEmail;
    @Column(name = "hausmeister_cc_email", length = 254)
    private String hausmeisterCcEmail;
    @Column(name = "hausmeister_automatisch")
    private Boolean hausmeisterAutomatisch;

    @jakarta.persistence.Column(name="antwort_test_email", length=254)
    private String antwortTestEmail;

    @jakarta.persistence.Column(name="antwort_cc_email", length=254)
    private String antwortCcEmail;


    @Column(name = "hausmeister_letzte_woche")
    private java.time.LocalDate hausmeisterLetzteWoche;

    @ElementCollection
    @CollectionTable(name = "homepage_anbindung_mietbereich",
            joinColumns = @JoinColumn(name = "mietobjekt_id"))
    @Column(name = "mietbereich_id", nullable = false)
    private Set<Long> erlaubteMietbereichIds = new HashSet<>();
}
